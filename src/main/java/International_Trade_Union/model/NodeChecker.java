package International_Trade_Union.model;

import International_Trade_Union.controllers.BasisController;
import International_Trade_Union.logger.MyLogger;
import International_Trade_Union.network.AllTransactions;
import International_Trade_Union.setings.Seting;
import International_Trade_Union.utils.UtilUrl;
import International_Trade_Union.utils.UtilsAllAddresses;
import International_Trade_Union.utils.UtilsJson;
import International_Trade_Union.utils.UtilsResolving;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class NodeChecker {

    private static final int CHECK_TIMEOUT = 4000; // Таймаут для проверки узла в миллисекундах

    public void checkNodes(UtilsResolving utilsResolving) throws InterruptedException, ExecutionException {
        BasisController.getBlockedNewSendBlock().set(false);

        // 1. Получаем исходный список узлов
        Set<String> nodes = BasisController.getNodes();
        List<HostEndDataShortB> hosts = utilsResolving.sortPriorityHost(nodes);
        Set<String> allNodes = new HashSet<>(nodes);

        // Получаем списки узлов от каждого сервера
        for (HostEndDataShortB hostEndDataShortB : hosts) {
            String s = hostEndDataShortB.getHost();
            try {
                String strNodes = UtilUrl.readJsonFromUrl(s + "/getNodes");
                Set<String> serverNodes = UtilsJson.jsonToSetAddresses(strNodes);
                allNodes.addAll(serverNodes);
            } catch (IOException | JSONException e) {
                MyLogger.saveLog("Error getting nodes from " + s + ": " + e.getMessage());
            }
        }

        // 2. Удаляем внутренний список
        Mining.deleteFiles(Seting.ORIGINAL_POOL_URL_ADDRESS_FILE);

        // 3. Сохраняем каждый адрес отдельно
        Set<String> uniqueNodes = allNodes.stream()
                .filter(node -> !nodes.contains(node))
                .collect(Collectors.toSet());

        Set<String> responsiveNodes = checkNodeReadiness(uniqueNodes);

        allNodes.addAll(responsiveNodes);

        // Сохраняем все узлы, включая первоначальные
        allNodes.addAll(nodes);

        for (String address : allNodes) {
                UtilsAllAddresses.putHost(address);

        }
    }

    private Set<String> checkNodeReadiness(Set<String> nodes) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<String>> futures = nodes.stream()
                .map(node -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String response = UtilUrl.readJsonFromUrl(node + "/confirmReadiness", CHECK_TIMEOUT);
                        return node; // Возвращаем узел, если он отвечает
                    } catch (Exception e) {
                        MyLogger.saveLog("Error checking readiness for " + node + ": " + e.getMessage());
                    }
                    return null; // Не добавляем узел, если он не отвечает
                }, executor))
                .collect(Collectors.toList());

        List<String> responsiveNodes = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .get();

        executor.shutdown();

        return new HashSet<>(responsiveNodes);
    }

    public void initiateProcess(List<HostEndDataShortB> sortPriorityHost) {


        // Потокобезопасный список для доступных узлов
        List<HostEndDataShortB> availableHosts = Collections.synchronizedList(new ArrayList<>());

        // Потокобезопасное множество для недоступных узлов
        Set<String> unresponsiveAddresses = Collections.synchronizedSet(new HashSet<>());

        // Проверяем состояние всех узлов
        List<CompletableFuture<Void>> checkFutures = sortPriorityHost.stream()
                .map(host -> CompletableFuture.runAsync(() -> {
                    boolean isResponding = false;
                    for (int attempt = 0; attempt < 3; attempt++) {
                        try {
                            String response = UtilUrl.readJsonFromUrl(host.getHost() + "/confirmReadiness", 7000);
                            isResponding = true;
                            if ("ready".equals(response)) {
                                synchronized (availableHosts) {
                                    availableHosts.add(host);
                                }
                                break;
                            }
                        } catch (java.net.ConnectException e) {
                            synchronized (unresponsiveAddresses) {
                                unresponsiveAddresses.add(extractHostPort(host.getHost()));
                            }
                            break; // Не нужно повторять попытки, если соединение отказано
                        } catch (Exception e) {
                        }
                    }
                    if (!isResponding) {
                        synchronized (unresponsiveAddresses) {
                            unresponsiveAddresses.add(extractHostPort(host.getHost()));
                        }
                    }
                }))
                .collect(Collectors.toList());

        // Ждем завершения всех проверок
        CompletableFuture.allOf(checkFutures.toArray(new CompletableFuture[0])).join();



        // Ограничиваем количество ожидаемых узлов до 7
        int nodesToWait = Math.min(availableHosts.size(), 7);

        if (nodesToWait == 0) {
            MyLogger.saveLog("No nodes to wait for, all are ready or unreachable");
            return;
        }


        CountDownLatch latch = new CountDownLatch(nodesToWait);

        // Теперь ждем, пока неготовые узлы станут готовыми
        List<CompletableFuture<Void>> waitFutures = availableHosts.stream()
                .map(host -> CompletableFuture.runAsync(() -> {
                    while (true) {
                        try {
                            String response = UtilUrl.readJsonFromUrl(host.getHost() + "/confirmReadiness", 2000);
                            if ("ready".equals(response)) {
                                latch.countDown();
                                break;
                            }
                            Thread.sleep(1000); // Пауза перед следующей проверкой
                        } catch (Exception e) {
                            MyLogger.saveLog("Error waiting for readiness of " + host.getHost() + ": " + e.getMessage());
                            latch.countDown(); // Уменьшаем счетчик, если узел стал недоступен
                            break;
                        }
                    }
                }))
                .collect(Collectors.toList());

        try {
            // Ждем максимум 25 секунд
            boolean completed = latch.await(25, TimeUnit.SECONDS);
            if (!completed) {
            }
        } catch (InterruptedException e) {
            MyLogger.saveLog("Waiting was interrupted: " + e.getMessage());
        }

        MyLogger.saveLog("finish: initiateProcess");
    }

    public String extractHostPort(String url) {
        try {
            java.net.URL netUrl = new java.net.URL(url);
            return netUrl.getHost() + ":" + netUrl.getPort();
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }
}
