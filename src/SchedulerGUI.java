
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SchedulerGUI extends JFrame {

    // Main colors
    private final Color NAVY = new Color(65, 55, 125);
    private final Color BACKGROUND = new Color(247, 245, 255);
    private final Color WHITE = Color.WHITE;
    private final Color BLUE = new Color(75, 125, 230);
    private final Color LIGHT_BLUE = new Color(225, 235, 255);
    private final Color TEXT = new Color(55, 55, 85);
    private final Color BORDER = new Color(225, 220, 245);

    private JComboBox<String> algorithmBox;

    private JTextField contextSwitchField;
    private JTextField quantumField;
    private JTextField agingField;

    private DefaultTableModel tableModel;
    private JTextArea outputArea;

    public SchedulerGUI() {

        setTitle("CPU Scheduling Simulator");
        setSize(950, 750);
        setMinimumSize(new Dimension(800, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(12, 12));

        // 1. Top panel: algorithm settings
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 12, 12));
        topPanel.setBackground(WHITE);
        topPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(18, 20, 18, 20)
                )
        );

        algorithmBox = new JComboBox<>(
                new String[]{"SRTF", "ACS", "Both"}
        );

        contextSwitchField = new JTextField("0");
        quantumField = new JTextField("3");
        agingField = new JTextField("4");

        addSetting(topPanel, "Algorithm:", algorithmBox);
        addSetting(topPanel, "Context Switch:", contextSwitchField);
        addSetting(topPanel, "Quantum:", quantumField);
        addSetting(topPanel, "Aging Interval:", agingField);

        add(topPanel, BorderLayout.NORTH);

        // 2. Center: processes table
        String[] columns = {
                "Process ID", "Arrival Time",
                "Burst Time", "Priority"
        };

        tableModel = new DefaultTableModel(columns, 0);

        JTable processTable = new JTable(tableModel);
        styleTable(processTable);

        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 12)
        );
        buttonPanel.setBackground(BACKGROUND);

        JButton addButton = new JButton("Add Process");
        JButton removeButton = new JButton("Remove Selected");
        JButton runButton = new JButton("Run Scheduler");

        styleButton(addButton, new Color(35, 175, 155));
        styleButton(removeButton, new Color(240, 105, 125));
        styleButton(runButton, new Color(110, 85, 220));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(runButton);

        // 4. Output area
        outputArea = new JTextArea(13, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setBackground(new Color(42, 43, 85));
        outputArea.setForeground(new Color(240, 240, 255));
        outputArea.setCaretColor(WHITE);
        outputArea.setMargin(new Insets(12, 15, 12, 15));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER),
                        "Scheduler Results"
                )
        );

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(BACKGROUND);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(outputScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> addProcess());

        removeButton.addActionListener(e -> removeProcess(processTable));

        runButton.addActionListener(e -> runScheduler());
    }

    // Style settings
    private void addSetting(JPanel panel, String title, JComponent field) {

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(NAVY);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (field instanceof JTextField) {
            field.setBackground(BACKGROUND);
            ((JTextField) field).setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    )
            );
        }

        panel.add(label);
        panel.add(field);
    }

    // Style process table
    private void styleTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setBackground(WHITE);
        table.setForeground(TEXT);
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(NAVY);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(NAVY);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
    }

    // Style buttons
    private void styleButton(JButton button, Color color) {

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addProcess() {

        int number = tableModel.getRowCount() + 1;

        tableModel.addRow(new Object[]{
                "P" + number, 0, 1, 1
        });
    }

    private void removeProcess(JTable table) {

        int selectedRow = table.getSelectedRow();

        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a process first.",
                    "No Process Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private List<Process> createProcesses() {

        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            String id = tableModel.getValueAt(i, 0).toString();

            int arrival = Integer.parseInt(
                    tableModel.getValueAt(i, 1).toString()
            );

            int burst = Integer.parseInt(
                    tableModel.getValueAt(i, 2).toString()
            );

            int priority = Integer.parseInt(
                    tableModel.getValueAt(i, 3).toString()
            );

            processes.add(
                    new Process(id, arrival, burst, priority)
            );
        }

        return processes;
    }

    private void runScheduler() {

        try {

            if (tableModel.getRowCount() == 0) {
                throw new IllegalArgumentException(
                        "Please add at least one process."
                );
            }

            int contextSwitch = Integer.parseInt(
                    contextSwitchField.getText()
            );

            String algorithm = (String) algorithmBox.getSelectedItem();

            outputArea.setText("");

            if (algorithm.equals("SRTF") || algorithm.equals("Both")) {

                List<Process> processes = createProcesses();

                SRTF srtf = new SRTF();

                srtf.schedule(processes, contextSwitch);

                displayResults(
                        "SRTF",
                        processes,
                        srtf.getTimeline(),
                        srtf.getContextSwitches()
                );
            }

            if (algorithm.equals("ACS") || algorithm.equals("Both")) {

                int quantum = Integer.parseInt(quantumField.getText());
                int agingInterval = Integer.parseInt(agingField.getText());

                List<Process> processes = createProcesses();

                ACS acs = new ACS(
                        quantum, contextSwitch, agingInterval
                );

                acs.schedule(processes);

                displayResults(
                        "ACS",
                        processes,
                        acs.getTimeline(),
                        acs.getContextSwitches()
                );

                outputArea.append("\n--- ACS Queue-State Matrix ---\n");

                for (String state : acs.getDemotionMatrix()) {
                    outputArea.append(state + "\n");
                }
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please check your inputs.\n" + ex.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void displayResults(
            String title,
            List<Process> processes,
            List<String> timeline,
            int contextSwitches) {

        outputArea.append("\n========== " + title + " RESULTS ==========\n");

        outputArea.append("\nTimeline:\n");

        for (int i = 0; i < timeline.size(); i++) {
            outputArea.append(i + " - " + timeline.get(i) + "\n");
        }

        outputArea.append("\nProcess Metrics:\n");

        outputArea.append(
                "Process | Waiting | Turnaround | Response\n"
        );

        for (Metrics.ProcessMetrics m : Metrics.calculateAll(processes)) {

            Process p = m.getProcess();

            outputArea.append(
                    p.getId() + " | " +
                            m.getWaitingTime() + " | " +
                            m.getTurnaroundTime() + " | " +
                            m.getResponseTime() + "\n"
            );
        }

        outputArea.append(
                "\nAverage Waiting Time: " +
                        Metrics.averageWaitingTime(processes) + "\n"
        );

        outputArea.append(
                "Average Turnaround Time: " +
                        Metrics.averageTurnaroundTime(processes) + "\n"
        );

        outputArea.append(
                "Average Response Time: " +
                        Metrics.averageResponseTime(processes) + "\n"
        );

        outputArea.append(
                "Context Switches: " + contextSwitches + "\n"
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            SchedulerGUI gui = new SchedulerGUI();
            gui.setVisible(true);
        });
    }
}
