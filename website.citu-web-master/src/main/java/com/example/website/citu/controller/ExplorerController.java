package com.example.website.citu.controller;

import com.example.website.citu.entity.SignRequest;
import com.example.website.citu.entity.SubBlockchainEntity;
import com.example.website.citu.model.Block;
import com.example.website.citu.model.DtoTransaction;
import com.example.website.citu.model.StatusTransaction;
import com.example.website.citu.utils.UtilUrl;
import com.example.website.citu.utils.UtilsJson;
import com.example.website.citu.utils.UtilsUse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ExplorerController {

    private static boolean IS_TEST = false;
    private static String address = IS_TEST ? "http://localhost:8083" : "http://194.87.236.238:82";

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        ExplorerController.address = address;
    }

    @GetMapping("/conductor")
    public String conductors(Model model, HttpSession session) throws IOException {
        populateConductorBase(model);
        model.addAttribute("status", "");
        return "conductor";
    }

    @GetMapping({"/blocks", "/conductor/blocks"})
    public String blocks(
            @RequestParam(required = false) Integer start,
            @RequestParam(required = false) Integer finish,
            Model model) throws IOException {
        Range range = start == null && finish == null ? latestRange() : manualRange(start, finish);
        return renderBlocks(range, model);
    }

    @PostMapping("/blocks")
    public String postBlocks(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int finish,
            Model model) throws IOException {
        return renderBlocks(manualRange(start, finish), model);
    }

    @PostMapping("/conductor/blocks")
    public String postConductorBlocks(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int finish,
            Model model) throws IOException {
        return renderBlocks(manualRange(start, finish), model);
    }

    @GetMapping("/conductor/lookup")
    public String lookup(@RequestParam(value = "index", defaultValue = "1") String index, Model model) throws IOException {
        populateConductorBase(model);
        List<Block> blocks = findBlocksByIndex(index);
        model.addAttribute("block", blocks.isEmpty() ? null : blocks.get(0));
        model.addAttribute("blocks", blocks);
        model.addAttribute("status", blocks.isEmpty() ? "not found" : "");
        model.addAttribute("duplicateNote", blocks.size() > 1 ? "Duplicate block indexes can exist; all matching blocks are shown below." : "");
        return "conductor";
    }

    @PostMapping("/conductor")
    public String conductor(
            @ModelAttribute SignRequest signRequest,
            @RequestParam(required = false) String page,
            RedirectAttributes redirectAttrs,
            HttpSession session) throws IOException {
        String info = signRequest.getSign();
        if (info == null || info.isEmpty()) {
            info = "0";
        }

        if (isNumeric(info)) {
            addBlockSearchResult(info, redirectAttrs);
        } else {
            addTransactionSearchResult(signRequest, redirectAttrs);
        }

        return "redirect:/conductor";
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("[-+]?\\d+(\\.\\d+)?");
    }

    @GetMapping({"detail-transactions", "/detail", "/details", "/detail-block"})
    public String details(Model model) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        model.addAttribute("title", "Block details");
        model.addAttribute("message", "Select a block or transaction from Explorer.");
        model.addAttribute("blocks", Collections.emptyList());
        model.addAttribute("transactions", Collections.emptyList());
        model.addAttribute("status", "");
        return "detail-transactions";
    }

    @GetMapping({"/detail-transactions/{index}", "/detail/{index}", "/details/{index}", "/detail-block/{index}"})
    public String detailTransactionIndex(@PathVariable(value = "index") String index, Model model) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        List<Block> blocks = isNumeric(index) ? findBlocksByIndex(index) : Collections.emptyList();
        Block block = blocks.isEmpty() ? null : blocks.get(0);
        List<DtoTransaction> transactions = block == null || block.getDtoTransactions() == null
                ? Collections.emptyList()
                : block.getDtoTransactions();
        model.addAttribute("title", "Block details");
        model.addAttribute("message", block == null ? "Block not found or temporarily unavailable." : "");
        model.addAttribute("block", block);
        model.addAttribute("blocks", blocks);
        model.addAttribute("transactions", transactions);
        model.addAttribute("status", block == null ? "not found" : "");
        model.addAttribute("duplicateNote", blocks.size() > 1 ? "Duplicate block indexes can exist; all matching blocks are shown below." : "");
        return "detail-transactions";
    }

    @GetMapping("detail-laws")
    public String detailsLaw(Model model) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        return "detail-laws";
    }

    @GetMapping("/detail-laws/{hex}")
    public String detailLaws(@PathVariable(value = "hex") String hex, RedirectAttributes redirectAttrs) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        byte[] sign = UtilsUse.hexToBytes(hex);
        String signStr = Base64.getEncoder().encodeToString(sign);
        String text = UtilUrl.getObject(address + "/conductorHashTran?hash=" + signStr);

        if (ApiFallback.isBlank(text)) {
            redirectAttrs.addFlashAttribute("laws", Collections.emptyList());
            redirectAttrs.addFlashAttribute("packageName", ApiFallback.UNAVAILABLE);
            redirectAttrs.addFlashAttribute("hashLaw", "");
            return "redirect:/detail-laws";
        }

        try {
            DtoTransaction dtoTransaction = (DtoTransaction) UtilsJson.jsonToClass(text, DtoTransaction.class);
            if (dtoTransaction.getLaws() == null) {
                redirectAttrs.addFlashAttribute("laws", Collections.emptyList());
                redirectAttrs.addFlashAttribute("packageName", ApiFallback.UNAVAILABLE);
                redirectAttrs.addFlashAttribute("hashLaw", "");
                return "redirect:/detail-laws";
            }
            List<String> allLaws = dtoTransaction.getLaws().getLaws() == null
                    ? Collections.emptyList()
                    : dtoTransaction.getLaws().getLaws();
            redirectAttrs.addFlashAttribute("laws", allLaws);
            redirectAttrs.addFlashAttribute("packageName", dtoTransaction.getLaws().getPacketLawName());
            redirectAttrs.addFlashAttribute("hashLaw", dtoTransaction.getLaws().getHashLaw());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("laws", Collections.emptyList());
            redirectAttrs.addFlashAttribute("packageName", ApiFallback.UNAVAILABLE);
            redirectAttrs.addFlashAttribute("hashLaw", "");
        }

        return "redirect:/detail-laws";
    }

    private List<Block> loadBlocks(int start, int finish) throws IOException {
        start = Math.max(0, start);
        finish = Math.max(start, finish);
        if (finish - start > 50) {
            finish = start + 50;
        }
        String subBlockchainJson = UtilsJson.objToStringJson(new SubBlockchainEntity(start, finish));
        String jsonList = UtilUrl.getObjectLongRead(subBlockchainJson, address + "/sub-blocks");
        return ApiFallback.blocks(jsonList).stream()
                .sorted(Comparator.comparing(Block::getIndex).reversed())
                .collect(Collectors.toList());
    }

    private String renderBlocks(Range range, Model model) throws IOException {
        List<Block> blocks = loadBlocks(range.start(), range.finish());
        model.addAttribute("title", "Blocks");
        model.addAttribute("blocks", blocks);
        model.addAttribute("start", range.start());
        model.addAttribute("finish", range.finish());
        model.addAttribute("blockCount", blocks.size());
        model.addAttribute("currentSize", range.currentSize());
        model.addAttribute("status", range.status().isEmpty() && blocks.isEmpty() ? ApiFallback.UNAVAILABLE : range.status());
        return "blocks";
    }

    private void populateConductorBase(Model model) throws IOException {
        Range range;
        List<Block> recentBlocks;
        try {
            range = latestRange();
            recentBlocks = loadBlocks(range.start(), range.finish());
        } catch (Exception e) {
            range = new Range(0, 10, -1, "latest blocks temporarily unavailable");
            recentBlocks = Collections.emptyList();
        }
        model.addAttribute("title", "Explorer");
        model.addAttribute("recentBlocks", recentBlocks);
        model.addAttribute("recentStart", range.start());
        model.addAttribute("recentFinish", range.finish());
        model.addAttribute("recentBlockCount", recentBlocks.size());
        model.addAttribute("recentStatus", recentBlocks.isEmpty() || !range.status().isEmpty() ? "latest blocks temporarily unavailable" : "");
        model.addAttribute("blocks", Collections.emptyList());
    }

    private Range latestRange() throws IOException {
        int currentSize = ApiFallback.intValue(UtilUrl.getObject(address + "/size", UtilUrl.EXPLORER_READ_TIMEOUT_MILLIS), -1);
        if (currentSize <= 0) {
            return new Range(0, 10, -1, "latest size temporarily unavailable; showing fallback range");
        }
        return new Range(Math.max(0, currentSize - 10), currentSize, currentSize, "");
    }

    private Range manualRange(Integer start, Integer finish) {
        int safeStart = Math.max(0, start == null ? 0 : start);
        int safeFinish = finish == null ? safeStart + 10 : Math.max(safeStart, finish);
        return new Range(safeStart, safeFinish, -1, "");
    }

    private record Range(int start, int finish, int currentSize, String status) {
    }

    private void addBlockSearchResult(String info, RedirectAttributes redirectAttrs) throws IOException {
        List<Block> blocks = findBlocksByIndex(info);
        Block block = blocks.isEmpty() ? null : blocks.get(0);
        if ("0".equals(info)) {
            redirectAttrs.addFlashAttribute("text", "The peculiarity of this blockchain is that the genesis block also has an index of 1, as does the block following it. This is normal, the entire block chain is correct and each block is unique, with unique content. Thus, in the blockchain, two blocks have identical indices, but different contents. This is simply a feature of this blockchain.\n");
        }
        redirectAttrs.addFlashAttribute("block", block);
        redirectAttrs.addFlashAttribute("blocks", blocks);
        redirectAttrs.addFlashAttribute("status", block == null ? "not found" : "");
    }

    private List<Block> findBlocksByIndex(String info) throws IOException {
        String text = UtilUrl.getObject(address + "/conductorBlock?index=" + info, UtilUrl.EXPLORER_READ_TIMEOUT_MILLIS);
        return ApiFallback.blockResults(text);
    }

    private void addTransactionSearchResult(SignRequest signRequest, RedirectAttributes redirectAttrs) throws IOException {
        Block block = null;
        String json64 = UtilUrl.getObject(UtilsJson.objToStringJson(signRequest), address + "/statusTransaction64");
        String json58 = UtilUrl.getObject(UtilsJson.objToStringJson(signRequest), address + "/statusTransaction58");
        String text64 = statusText(json64);
        String text58 = statusText(json58);
        String text = "";

        if (text64.equals("absent") || text58.equals("absent")) {
            text = "absent";
        }
        if (!text64.equals("absent") && !text64.isEmpty()) {
            text = text64;
            if (text64.equals("success")) {
                block = firstBlock(UtilUrl.getObject(UtilsJson.objToStringJson(signRequest), address + "/findBlocksFromSign64"));
            }
        }
        if (!text58.equals("absent") && !text58.isEmpty()) {
            text = text58;
            if (text58.equals("success")) {
                block = firstBlock(UtilUrl.getObject(UtilsJson.objToStringJson(signRequest), address + "/findBlocksFromSign58"));
            }
        }

        redirectAttrs.addFlashAttribute("status", text.isEmpty() ? ApiFallback.UNAVAILABLE : text);
        redirectAttrs.addFlashAttribute("block", block);
    }

    private String statusText(String json) {
        StatusTransaction statusTransaction = ApiFallback.statusTransaction(json);
        return statusTransaction == null || statusTransaction.getStatus() == null ? "" : statusTransaction.getStatus();
    }

    private Block firstBlock(String json) {
        List<Block> blocks = new ArrayList<>(ApiFallback.blockList(json));
        return blocks.isEmpty() ? null : blocks.get(0);
    }
}
