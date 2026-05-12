package com.example.website.citu.controller;

import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
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
            Model redirectAttrs
    ) throws IOException {

        if (httpSession.getAttribute("pubkey") != null) {
            pubkey = (String) httpSession.getAttribute("pubkey");
        }
        if (httpSession.getAttribute("page") != null) {
            page = (int) httpSession.getAttribute("page");
        }

        if (httpSession.getAttribute("size") != null) {
            size = (int) httpSession.getAttribute("size");
        }

        int from = page * size;
        int to = from + size;

        String url = address + "/senderTransactions?address=" + pubkey + "&from=" + from + "&to=" + to;
        String text = UtilUrl.getObject(url);
        List<DtoTransaction> list =
                UtilsJson.jsonToListDto(text);

        url = address + "/senderCountDto?address=" + pubkey;
        long totalPages = (long) UtilsJson.jsonToClass(UtilUrl.getObject(url), Long.class);

        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);

        redirectAttrs.addAttribute("transactions", list);
        redirectAttrs.addAttribute("page", page);

        redirectAttrs.addAttribute("totalPages", totalPages);


        redirectAttrs.addAttribute("pubkey", pubkey);
        return "transactionsSender";

    }

    @GetMapping("/transactionsSender")
    public String transactions(
            HttpSession httpSession,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "pubkey") String pubkey,
            Model model
    ) throws IOException {
        if (httpSession.getAttribute("pubkey") != null) {
            pubkey = (String) httpSession.getAttribute("pubkey");
        }
        if (httpSession.getAttribute("page") != null) {
            page = (int) httpSession.getAttribute("page");
        }

        if (httpSession.getAttribute("size") != null) {
            size = (int) httpSession.getAttribute("size");
        }



        if (page < 0) {
            page = 0;
        }


        String url = "";
        String text = "";
        List<DtoTransaction> list = null;

        if (httpSession.getAttribute("transactions") != null) {
            list = (List<DtoTransaction>) httpSession.getAttribute("transactions");
        }else {
            url = address + "/senderTransactions?address=" + pubkey + "&from=" + page + "&to=" + size;
            text = UtilUrl.getObject(url);
            list = UtilsJson.jsonToListDto(text);
        }

        url = address + "/senderCountDto?address=" + pubkey;
        long totalPages = (long) UtilsJson.jsonToClass(UtilUrl.getObject(url), Long.class);

        // здесь можно получить данные для текущей страницы
        // на основе page и status
        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pubkey", pubkey);
        model.addAttribute("transactions", list);

        return "transactionsSender";

    }

    @PostMapping("/transactionsSender")
    public String transactions(HttpSession httpSession,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,

                               @RequestParam(name = "pubkey") String pubkey,
                               RedirectAttributes redirectAttrs) throws IOException {
        if (httpSession.getAttribute("pubkey") != null) {
            pubkey = (String) httpSession.getAttribute("pubkey");
        }
        if (httpSession.getAttribute("page") != null) {
            page = (int) httpSession.getAttribute("page");
        }

        if (httpSession.getAttribute("size") != null) {
            size = (int) httpSession.getAttribute("size");
        }


        if (page < 0) {
            page = 0;
        }


        String url = "";
        String text = "";
        List<DtoTransaction> list = null;


        url = address + "/senderTransactions?address=" + pubkey + "&from=" + page + "&to=" + size;
        text = UtilUrl.getObject(url);
        list = UtilsJson.jsonToListDto(text);

        url = address + "/senderCountDto?address=" + pubkey;
        long totalTransactions = (long) UtilsJson.jsonToClass(UtilUrl.getObject(url), Long.class);

        if (page < 0) {
            page = 0;
        }

        // здесь можно получить данные для текущей страницы
        // на основе page и status
        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);

        redirectAttrs.addAttribute("page", page);
        redirectAttrs.addAttribute("size", size);
        redirectAttrs.addAttribute("totalPages", totalTransactions);
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

        if(httpSession.getAttribute("pubkey") != null){
            pubkey = (String) httpSession.getAttribute("pubkey");
        }
        if (httpSession.getAttribute("page") != null){
            page = (int) httpSession.getAttribute("page");
        }

        if(httpSession.getAttribute("size") != null){
            size = (int) httpSession.getAttribute("size");
        }


        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);

        switch (action) {
            case "/send":
                return send(pubkey, page,  size,  redirectAttrs, httpSession);
            case "/prev":
                return prev(pubkey, page,  size,  redirectAttrs,  httpSession);
            case "/next":
                return next(pubkey, page,  size, redirectAttrs, httpSession);
            default:
                return "redirect:/";
        }
    }

    public String send(String pubkey,
                       int page,
                       int size,
                       RedirectAttributes redirectAttrs,

                       HttpSession httpSession) throws IOException {
        int from = 0;
        int to = 0;

        from = page * size;
        to = (page + 1) * page;
        httpSession.setAttribute("page", from);
        httpSession.setAttribute("size", to);



        String str = "";
        str = "/senderTransactions?address=";
        System.out.println("***********************************");
        System.out.println("from: " + from);
        System.out.println("to: " + to);

        String urlCount = address + "/senderCountDto?address=" + pubkey;
        long totalPages = (long) UtilsJson.jsonToClass(UtilUrl.getObject(urlCount), Long.class);

        if(page < 0){
            page = 0;
        }

        if(to == 0){
            to = page + 10;
        }

        if(to > totalPages){
            to = (int) totalPages;
        }

        String url = address +str+ pubkey + "&from=" + from + "&to=" + to;

        System.out.println("url: " + url);
        System.out.println("***********************************");

        String text = UtilUrl.getObject( url );
        List<DtoTransaction> list =
                UtilsJson.jsonToListDto(text);




        httpSession.setAttribute("transactions", list);
        System.out.println("totalPages: " + totalPages);


        redirectAttrs.addAttribute("page", page);
        redirectAttrs.addAttribute("size", size);
        redirectAttrs.addAttribute("totalPages", totalPages);
        redirectAttrs.addAttribute("pubkey", pubkey);

        httpSession.setAttribute("pubkey", pubkey);
        httpSession.setAttribute("page", page);
        httpSession.setAttribute("size", size);
        httpSession.setAttribute("totalPages", totalPages);
        return "redirect:/transactionsSender";
    }

    public String prev(String sender,
                       int page,
                       int size,
                       RedirectAttributes redirectAttrs, HttpSession httpSession) throws IOException {
            page = Math.max(0, page - 1);
        return send(sender, page,  size,  redirectAttrs, httpSession);
    }

    public String next(String sender,
                       int page,
                       int size,
                        RedirectAttributes redirectAttrs,  HttpSession httpSession) throws IOException {
            page = page + 1;
        return send(sender, page, size, redirectAttrs, httpSession);
    }
}
