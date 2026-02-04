package com.smartbudget.app.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mock blockchain ledger for secure financial data.
 */
public class BlockchainLedger {

    public static class Block {
        public String hash;
        public String previousHash;
        public String data;
        public long timeStamp;
        public int nonce;

        public Block(String data, String previousHash) {
            this.data = data;
            this.previousHash = previousHash;
            this.timeStamp = new Date().getTime();
            this.hash = calculateHash();
        }

        public String calculateHash() {
            String input = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + data;
            return applySha256(input);
        }

        public void mineBlock(int difficulty) {
            String target = new String(new char[difficulty]).replace('\0', '0');
            while (!hash.substring(0, difficulty).equals(target)) {
                nonce++;
                hash = calculateHash();
            }
        }
    }

    private static ArrayList<Block> blockchain = new ArrayList<>();
    private static int difficulty = 2; // Keep it low for mobile performance

    static {
        // Genesis block
        blockchain.add(new Block("Genesis Decision", "0"));
    }

    /**
     * Add spending transaction to the ledger.
     */
    public static void addTransaction(String transactionData) {
        Block newBlock = new Block(transactionData, blockchain.get(blockchain.size() - 1).hash);
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    /**
     * Verify chain integrity.
     */
    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Compare registered hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                return false;
            }

            // Compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                return false;
            }

            // Check if block is mined
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                return false;
            }
        }
        return true;
    }

    public static String getLatestHash() {
        return blockchain.get(blockchain.size() - 1).hash;
    }

    public static int getBlockCount() {
        return blockchain.size();
    }

    private static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
