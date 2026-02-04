package com.smartbudget.app.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Bill splitter helper.
 * Split expenses between friends/groups.
 */
public class BillSplitter {

    public static class Participant {
        public String name;
        public double amountPaid;
        public double amountOwed;
        public double balance; // + means they should receive, - means they should pay

        public Participant(String name) {
            this.name = name;
            this.amountPaid = 0;
            this.amountOwed = 0;
            this.balance = 0;
        }
    }

    public static class Settlement {
        public String from;
        public String to;
        public double amount;

        public Settlement(String from, String to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("#,###");
            return String.format("%s → %s: %s₫", from, to, df.format(amount));
        }
    }

    /**
     * Split bill equally among participants.
     */
    public static List<Participant> splitEqually(double totalAmount, List<String> names) {
        List<Participant> participants = new ArrayList<>();
        double perPerson = totalAmount / names.size();

        for (String name : names) {
            Participant p = new Participant(name);
            p.amountOwed = perPerson;
            participants.add(p);
        }

        return participants;
    }

    /**
     * Split bill with custom amounts.
     */
    public static List<Participant> splitCustom(List<String> names, List<Double> amountsPaid, 
                                                 List<Double> amountsOwed) {
        List<Participant> participants = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            Participant p = new Participant(names.get(i));
            p.amountPaid = amountsPaid.get(i);
            p.amountOwed = amountsOwed.get(i);
            p.balance = p.amountPaid - p.amountOwed;
            participants.add(p);
        }

        return participants;
    }

    /**
     * Calculate settlements to balance the bill.
     */
    public static List<Settlement> calculateSettlements(List<Participant> participants) {
        List<Settlement> settlements = new ArrayList<>();
        
        // Calculate balances
        List<Participant> creditors = new ArrayList<>(); // Should receive money
        List<Participant> debtors = new ArrayList<>();   // Should pay money

        for (Participant p : participants) {
            if (p.balance > 0.01) {
                creditors.add(p);
            } else if (p.balance < -0.01) {
                debtors.add(p);
            }
        }

        // Sort for optimal matching
        creditors.sort((a, b) -> Double.compare(b.balance, a.balance));
        debtors.sort((a, b) -> Double.compare(a.balance, b.balance));

        // Create settlements
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            Participant creditor = creditors.get(i);
            Participant debtor = debtors.get(j);

            double amount = Math.min(creditor.balance, -debtor.balance);
            
            if (amount > 0.01) {
                settlements.add(new Settlement(debtor.name, creditor.name, amount));
            }

            creditor.balance -= amount;
            debtor.balance += amount;

            if (Math.abs(creditor.balance) < 0.01) i++;
            if (Math.abs(debtor.balance) < 0.01) j++;
        }

        return settlements;
    }

    /**
     * Split with tip.
     */
    public static double calculateWithTip(double subtotal, double tipPercent) {
        return subtotal * (1 + tipPercent / 100);
    }

    /**
     * Get formatted summary.
     */
    public static String getSummary(double total, int numPeople, List<Settlement> settlements) {
        DecimalFormat df = new DecimalFormat("#,###");
        StringBuilder sb = new StringBuilder();
        
        sb.append("💰 Tổng: ").append(df.format(total)).append("₫\n");
        sb.append("👥 ").append(numPeople).append(" người\n");
        sb.append("💵 Mỗi người: ").append(df.format(total / numPeople)).append("₫\n\n");
        
        if (!settlements.isEmpty()) {
            sb.append("📋 Thanh toán:\n");
            for (Settlement s : settlements) {
                sb.append("• ").append(s.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
