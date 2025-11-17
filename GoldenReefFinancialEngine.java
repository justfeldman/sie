import java.util.ArrayList;
import java.util.List;

public class GoldenReefFinancialEngine {

    /* ============================================================
     *  EXPENSE MODEL
     * ============================================================ */
    public static class Expense {
        public enum Category { COGS, MARKETING, OPS, TECH_AR, TECH_AI, TECH_BACKEND }
        private final Category category;
        private final double amount;

        public Expense(Category category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public Category getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    /* ============================================================
     *  EXPENSE MANAGER
     * ============================================================ */
    public static class ExpenseManager {
        private final List<Expense> expenses = new ArrayList<>();

        public void addExpense(Expense e) { expenses.add(e); }

        public double getTotalByCategory(Expense.Category c) {
            return expenses.stream()
                    .filter(e -> e.getCategory() == c)
                    .mapToDouble(Expense::getAmount)
                    .sum();
        }

        public double getTotalTechCosts() {
            return getTotalByCategory(Expense.Category.TECH_AR)
                 + getTotalByCategory(Expense.Category.TECH_AI)
                 + getTotalByCategory(Expense.Category.TECH_BACKEND);
        }

        public double getTotalExpenses() {
            return expenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();
        }
    }

    /* ============================================================
     *  REVENUE ENGINE (forecast + margin)
     * ============================================================ */
    public static class RevenueEngine {
        public double forecastRevenue(double units, double aov, double cagr) {
            return units * aov * (1 + cagr);
        }

        public double margin(double revenue, double cogs) {
            return revenue - cogs;
        }
    }

    /* ============================================================
     *  R&D ENGINE (43.5% rebate calculator)
     * ============================================================ */
    public static class RnDEngine {
        private static final double REBATE_RATE = 0.435;

        public double rebate(double techSpend, double eligibilityPct) {
            return techSpend * eligibilityPct * REBATE_RATE;
        }
    }

    /* ============================================================
     *  CASH FLOW ENGINE
     * ============================================================ */
    public static class CashFlowEngine {
        public double computeCashFlow(double revenue, double expenses, double rebate) {
            return revenue - expenses + rebate;
        }

        public double runway(double cash, double monthlyBurn) {
            if (monthlyBurn <= 0) return Double.POSITIVE_INFINITY;
            return cash / monthlyBurn;
        }
    }

    /* ============================================================
     *  VALUATION ENGINE (5–7× rev + DCF @ 4.2%)
     * ============================================================ */
    public static class ValuationEngine {
        public double revenueMultiple(double revenue) {
            return revenue * 6; // midpoint of 5–7× range
        }

        public double dcf(double cashFlow, double cagr) {
            return cashFlow / (cagr + 0.01); // terminal value simplified
        }
    }

    /* ============================================================
     *  MASTER CONTROLLER (Excel Architecture in Java)
     * ============================================================ */
    private final ExpenseManager expenseManager = new ExpenseManager();
    private final RevenueEngine revenueEngine = new RevenueEngine();
    private final RnDEngine rndEngine = new RnDEngine();
    private final CashFlowEngine cashFlowEngine = new CashFlowEngine();
    private final ValuationEngine valuationEngine = new ValuationEngine();

    public void addExpense(Expense e) {
        expenseManager.addExpense(e);
    }

    public void run(
            double units,
            double aov,
            double cagr,
            double rAndDEligibility,
            double currentCash
    ) {

        // Revenue + margin
        double revenue = revenueEngine.forecastRevenue(units, aov, cagr);
        double cogs = expenseManager.getTotalByCategory(Expense.Category.COGS);
        double margin = revenueEngine.margin(revenue, cogs);

        // Tech + total expenses
        double techSpend = expenseManager.getTotalTechCosts();
        double totalExpenses = expenseManager.getTotalExpenses();

        // R&D rebate
        double rebate = rndEngine.rebate(techSpend, rAndDEligibility);

        // Cash flow + runway
        double cashFlow = cashFlowEngine.computeCashFlow(revenue, totalExpenses, rebate);
        double runway = cashFlowEngine.runway(currentCash, totalExpenses / 12);

        // Valuations
        double valuation = valuationEngine.revenueMultiple(revenue);
        double dcf = valuationEngine.dcf(cashFlow, cagr);

        // Output results
        System.out.println("========= GOLDEN REEF FINANCIAL MODEL =========");
        System.out.println("Revenue: $" + revenue);
        System.out.println("COGS: $" + cogs);
        System.out.println("Margin: $" + margin);
        System.out.println("Total Tech Spend: $" + techSpend);
        System.out.println("Total Expenses: $" + totalExpenses);
        System.out.println("R&D Rebate (43.5%): $" + rebate);
        System.out.println("-----------------------------------------------");
        System.out.println("Cash Flow: $" + cashFlow);
        System.out.println("Runway (months): " + runway);
        System.out.println("Valuation (5–7× Revenue): $" + valuation);
        System.out.println("DCF Valuation: $" + dcf);
        System.out.println("===============================================");
    }

    /* ============================================================
     *  EXAMPLE EXECUTION (MVP)
     * ============================================================ */
    public static void main(String[] args) {

        GoldenReefFinancialEngine model = new GoldenReefFinancialEngine();

        model.addExpense(new Expense(Expense.Category.COGS, 6000));
        model.addExpense(new Expense(Expense.Category.MARKETING, 3500));
        model.addExpense(new Expense(Expense.Category.OPS, 2500));
        model.addExpense(new Expense(Expense.Category.TECH_AR, 7000));
        model.addExpense(new Expense(Expense.Category.TECH_AI, 4000));
        model.addExpense(new Expense(Expense.Category.TECH_BACKEND, 9000));

        model.run(
                1000,      // units sold
                85,        // AOV
                0.042,     // CAGR 4.2%
                0.70,      // 70% R&D eligible
                30000      // current cash
        );
    }
}
