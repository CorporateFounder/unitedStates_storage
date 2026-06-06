package com.example.website.citu.controller;

import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.utils.UtilUrl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class TransactionCustomerController {
    String address = ExplorerController.getAddress();
    private String lastRawResponse = "";

    @GetMapping({"/customer", "/customerTransactions"})
    public String senderDto(
            HttpSession httpSession,
            @RequestParam(name = "pubkey", required = false) String pubkey,
            @RequestParam(name = "address", required = false) String queryAddress,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) throws IOException {
        return renderTransactions(httpSession, firstText(pubkey, queryAddress), from, to, page, size, model);
    }

    @GetMapping("/transactionsCustomer")
    public String transactions(
            HttpSession httpSession,
            @RequestParam(name = "pubkey", required = false) String pubkey,
            @RequestParam(name = "address", required = false) String queryAddress,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) throws IOException {
        return renderTransactions(httpSession, firstText(pubkey, queryAddress), from, to, page, size, model);
    }

    @PostMapping("/transactionsCustomer")
    public String transactions(HttpSession httpSession,
                               @RequestParam(defaultValue = "0") int from,
                               @RequestParam(defaultValue = "10") int to,
                               @RequestParam(name = "pubkey", required = false) String pubkey,
                               @RequestParam(name = "address", required = false) String queryAddress,
                               RedirectAttributes redirectAttrs) {
        Values values = normalize(httpSession, firstText(pubkey, queryAddress), from, to);
        addRedirectValues(redirectAttrs, values);
        return "redirect:/transactionsCustomer";
    }

    @PostMapping("/customer")
    public String senderDto(
            HttpSession httpSession,
            @RequestParam(name = "pubkey", required = false) String pubkey,
            @RequestParam(name = "address", required = false) String queryAddress,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int to,
            @RequestParam(defaultValue = "/send") String action,
            RedirectAttributes redirectAttrs
    ) throws IOException {
        Values values = normalize(httpSession, firstText(pubkey, queryAddress), from, to);
        switch (action) {
            case "/send":
                return send(values.address(), values.from(), values.to(), redirectAttrs, httpSession);
            case "/prev":
                return prev(values.address(), values.from(), values.to(), redirectAttrs, httpSession);
            case "/next":
                return next(values.address(), values.from(), values.to(), redirectAttrs, httpSession);
            default:
                return "redirect:/";
        }
    }

    public String send(String pubkey,
                       int from,
                       int to,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        Values values = normalize(httpSession, pubkey, from, to);
        List<DtoTransaction> list = customerTransactions(values.address(), values.from(), values.to());
        httpSession.setAttribute("transactions", list);
        httpSession.setAttribute("status", statusFor(lastRawResponse, list));
        addRedirectValues(redirectAttrs, values);
        return "redirect:/transactionsCustomer";
    }

    public String prev(String sender,
                       int from,
                       int to,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        int size = Math.max(1, to - from);
        return send(sender, Math.max(0, from - size), Math.max(size, to - size), redirectAttrs, httpSession);
    }

    public String next(String sender,
                       int from,
                       int to,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        int size = Math.max(1, to - from);
        return send(sender, from + size, to + size, redirectAttrs, httpSession);
    }

    private String renderTransactions(HttpSession httpSession, String requestedAddress, int from, int to, int page, int size, Model model) throws IOException {
        Values values = normalize(httpSession, requestedAddress, rangeStart(from, page, size), rangeEnd(from, to, page, size));
        List<DtoTransaction> list;
        String status;
        if (httpSession.getAttribute("transactions") instanceof List<?> sessionList && requestedAddress == null) {
            list = (List<DtoTransaction>) sessionList;
            status = String.valueOf(httpSession.getAttribute("status") == null ? "" : httpSession.getAttribute("status"));
        } else {
            list = customerTransactions(values.address(), values.from(), values.to());
            status = statusFor(lastRawResponse, list);
        }

        model.addAttribute("title", "Customer transactions");
        model.addAttribute("transactions", list);
        model.addAttribute("status", status);
        model.addAttribute("from", values.from());
        model.addAttribute("to", values.to());
        model.addAttribute("page", values.from());
        model.addAttribute("size", values.to());
        model.addAttribute("totalPages", 0);
        model.addAttribute("pubkey", values.address());
        model.addAttribute("address", values.address());
        model.addAttribute("mode", "customer");
        addPagingModel(model, values);
        return "transactionsCustomer";
    }

    private List<DtoTransaction> customerTransactions(String pubkey, int from, int to) throws IOException {
        if (ApiFallback.isBlank(pubkey)) {
            lastRawResponse = "";
            return List.of();
        }
        String url = address + "/customerTransactions?address=" + encode(pubkey) + "&from=" + from + "&to=" + to;
        lastRawResponse = UtilUrl.getObject(url, UtilUrl.EXPLORER_READ_TIMEOUT_MILLIS);
        return ApiFallback.transactions(lastRawResponse);
    }

    private Values normalize(HttpSession httpSession, String requestedAddress, int from, int to) {
        String selectedAddress = firstText(requestedAddress, (String) httpSession.getAttribute("pubkey"));
        from = Math.max(0, from);
        to = to <= from ? from + 10 : Math.min(from + 100, to);
        httpSession.setAttribute("pubkey", selectedAddress);
        httpSession.setAttribute("address", selectedAddress);
        httpSession.setAttribute("from", from);
        httpSession.setAttribute("to", to);
        return new Values(selectedAddress, from, to);
    }

    private int rangeStart(int from, int page, int size) {
        if (from > 0) {
            return from;
        }
        return Math.max(0, page * Math.max(1, size));
    }

    private int rangeEnd(int from, int to, int page, int size) {
        if (to > from) {
            return to;
        }
        int start = rangeStart(from, page, size);
        return start + Math.max(1, size);
    }

    private String statusFor(String raw, List<DtoTransaction> transactions) {
        if (ApiFallback.isBlank(raw)) {
            return ApiFallback.UNAVAILABLE;
        }
        return transactions.isEmpty() ? "not found" : "";
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void addRedirectValues(RedirectAttributes redirectAttrs, Values values) {
        redirectAttrs.addAttribute("pubkey", values.address());
        redirectAttrs.addAttribute("address", values.address());
        redirectAttrs.addAttribute("from", values.from());
        redirectAttrs.addAttribute("to", values.to());
    }

    private void addPagingModel(Model model, Values values) {
        int size = Math.max(1, values.to() - values.from());
        int prevFrom = Math.max(0, values.from() - size);
        int prevTo = values.from();
        model.addAttribute("prevFrom", prevFrom);
        model.addAttribute("prevTo", prevTo);
        model.addAttribute("nextFrom", values.to());
        model.addAttribute("nextTo", values.to() + size);
    }

    private record Values(String address, int from, int to) {
    }
}
