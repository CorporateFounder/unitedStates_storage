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
import java.util.List;

@Controller
public class TransactionSenderController {
    String address = ExplorerController.getAddress();

    @GetMapping("/sender")
    public String senderDto(
            HttpSession httpSession,
            @RequestParam(name = "pubkey") String pubkey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) throws IOException {
        return renderTransactions(httpSession, pubkey, page, size, model);
    }

    @GetMapping("/transactionsSender")
    public String transactions(
            HttpSession httpSession,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "pubkey") String pubkey,
            Model model
    ) throws IOException {
        return renderTransactions(httpSession, pubkey, page, size, model);
    }

    @PostMapping("/transactionsSender")
    public String transactions(HttpSession httpSession,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(name = "pubkey") String pubkey,
                               RedirectAttributes redirectAttrs) {
        normalizeSession(httpSession, pubkey, page, size);
        redirectAttrs.addAttribute("page", Math.max(page, 0));
        redirectAttrs.addAttribute("size", size);
        redirectAttrs.addAttribute("totalPages", 0);
        redirectAttrs.addAttribute("pubkey", pubkey);
        return "redirect:/transactionsSender";
    }

    @PostMapping("/sender")
    public String senderDto(
            HttpSession httpSession,
            @RequestParam(name = "pubkey") String pubkey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String action,
            RedirectAttributes redirectAttrs
    ) throws IOException {
        Values values = normalizeSession(httpSession, pubkey, page, size);
        switch (action) {
            case "/send":
                return send(values.pubkey(), values.page(), values.size(), redirectAttrs, httpSession);
            case "/prev":
                return prev(values.pubkey(), values.page(), values.size(), redirectAttrs, httpSession);
            case "/next":
                return next(values.pubkey(), values.page(), values.size(), redirectAttrs, httpSession);
            default:
                return "redirect:/";
        }
    }

    public String send(String pubkey,
                       int page,
                       int size,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        page = Math.max(page, 0);
        int from = page * size;
        int to = from + size;
        long totalPages = totalTransactions(pubkey);
        if (totalPages > 0 && to > totalPages) {
            to = (int) totalPages;
        }

        List<DtoTransaction> list = senderTransactions(pubkey, from, to);
        httpSession.setAttribute("transactions", list);
        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);
        httpSession.setAttribute("totalPages", totalPages);

        redirectAttrs.addAttribute("page", page);
        redirectAttrs.addAttribute("size", size);
        redirectAttrs.addAttribute("totalPages", totalPages);
        redirectAttrs.addAttribute("pubkey", pubkey);
        return "redirect:/transactionsSender";
    }

    public String prev(String sender,
                       int page,
                       int size,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        return send(sender, Math.max(0, page - 1), size, redirectAttrs, httpSession);
    }

    public String next(String sender,
                       int page,
                       int size,
                       RedirectAttributes redirectAttrs,
                       HttpSession httpSession) throws IOException {
        return send(sender, page + 1, size, redirectAttrs, httpSession);
    }

    private String renderTransactions(HttpSession httpSession, String pubkey, int page, int size, Model model) throws IOException {
        Values values = normalizeSession(httpSession, pubkey, page, size);
        int from = values.page() * values.size();
        int to = from + values.size();
        List<DtoTransaction> list = senderTransactions(values.pubkey(), from, to);
        long totalTransactions = totalTransactions(values.pubkey());

        model.addAttribute("transactions", list);
        model.addAttribute("page", values.page());
        model.addAttribute("size", values.size());
        model.addAttribute("totalPages", totalTransactions);
        model.addAttribute("pubkey", values.pubkey());
        return "transactionsSender";
    }

    private List<DtoTransaction> senderTransactions(String pubkey, int from, int to) throws IOException {
        String url = address + "/senderTransactions?address=" + pubkey + "&from=" + from + "&to=" + to;
        return ApiFallback.transactions(UtilUrl.getObject(url));
    }

    private long totalTransactions(String pubkey) throws IOException {
        String url = address + "/senderCountDto?address=" + pubkey;
        return ApiFallback.longValue(UtilUrl.getObject(url), 0);
    }

    private Values normalizeSession(HttpSession httpSession, String pubkey, int page, int size) {
        if (httpSession.getAttribute("pubkey") != null) {
            pubkey = (String) httpSession.getAttribute("pubkey");
        }
        if (httpSession.getAttribute("page") != null) {
            page = (int) httpSession.getAttribute("page");
        }
        if (httpSession.getAttribute("size") != null) {
            size = (int) httpSession.getAttribute("size");
        }
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : size;
        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);
        return new Values(pubkey, page, size);
    }

    private record Values(String pubkey, int page, int size) {
    }
}
