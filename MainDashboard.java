import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class MainDashboard extends JFrame {
    
    private String adminName;
    private StudentDAO studentDAO;
    private AdminDAO adminDAO; 
    
    private JComboBox<String> globalDivisionToggle;
    private JPanel dynamicContentPanel;
    private CardLayout cardLayout;
    
    // Global containers for reactive refreshing
    private JPanel homeContainer;
    private JTable scheduleTable;

    public MainDashboard(String adminName) {
        this.adminName = adminName;
        this.studentDAO = new StudentDAO();
        this.adminDAO = new AdminDAO();
        
        setTitle("Enterprise SMS - Admin Dashboard");
        setSize(1100, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. THE TOP BAR (DIVISION TOGGLE)
        // ==========================================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel welcomeLabel = new JLabel("Welcome, Prof. " + adminName);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topBar.add(welcomeLabel, BorderLayout.WEST);

        JPanel togglePanel = new JPanel();
        togglePanel.setOpaque(false);
        JLabel divLabel = new JLabel("Active View: ");
        divLabel.setForeground(Color.WHITE);
        
        String[] divisions = {"All Divisions", "Div A", "Div B", "Div C"};
        globalDivisionToggle = new JComboBox<>(divisions);
        globalDivisionToggle.addActionListener(e -> refreshDataForDivision());
        
        togglePanel.add(divLabel);
        togglePanel.add(globalDivisionToggle);
        topBar.add(togglePanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ==========================================
        // 2. SIDE NAVIGATION MENU
        // ==========================================
        JPanel sideMenu = new JPanel(new GridLayout(7, 1, 5, 5));
        sideMenu.setPreferredSize(new Dimension(220, 0));
        sideMenu.setBackground(new Color(44, 62, 80)); 
        sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        JButton btnHome = createMenuButton("Dashboard & Schedule");
        JButton btnStudents = createMenuButton("Manage Students");
        JButton btnAssignments = createMenuButton("Assignments");
        JButton btnQueries = createMenuButton("Student Queries");
        JButton btnReports = createMenuButton("Report Cards");

        sideMenu.add(btnHome);
        sideMenu.add(btnStudents);
        sideMenu.add(btnAssignments);
        sideMenu.add(btnQueries);
        sideMenu.add(btnReports);
        
        add(sideMenu, BorderLayout.WEST);

        // ==========================================
        // 3. DYNAMIC WORKSPACE (CARD LAYOUT)
        // ==========================================
        cardLayout = new CardLayout();
        dynamicContentPanel = new JPanel(cardLayout);
        
        // Wrap the home panel so we can easily wipe and refresh it later
        homeContainer = new JPanel(new BorderLayout());
        homeContainer.add(createHomePanel(), BorderLayout.CENTER);
        
        // Load all the panels into the CardLayout
        dynamicContentPanel.add(homeContainer, "Home"); 
        dynamicContentPanel.add(createScheduleFormPanel(), "ScheduleForm");
        dynamicContentPanel.add(createStudentsWorkspace(), "Students"); 
        dynamicContentPanel.add(createAssignmentPanel(), "Assign"); 
        dynamicContentPanel.add(createQueriesPanel(), "Queries");   
        dynamicContentPanel.add(createReportPanel(), "Reports");    

        add(dynamicContentPanel, BorderLayout.CENTER);

        // Map menu buttons to panels
        btnHome.addActionListener(e -> cardLayout.show(dynamicContentPanel, "Home"));
        btnStudents.addActionListener(e -> cardLayout.show(dynamicContentPanel, "Students"));
        btnAssignments.addActionListener(e -> cardLayout.show(dynamicContentPanel, "Assign"));
        btnQueries.addActionListener(e -> cardLayout.show(dynamicContentPanel, "Queries"));
        btnReports.addActionListener(e -> cardLayout.show(dynamicContentPanel, "Reports"));
    }

    // --- HELPER METHODS ---
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        return btn;
    }

    private void refreshDataForDivision() {
        String selectedDiv = (String) globalDivisionToggle.getSelectedItem();
        if (scheduleTable != null) {
            DefaultTableModel filteredModel = adminDAO.getAdminSchedule(adminName, selectedDiv);
            scheduleTable.setModel(filteredModel);
        }
    }

    private void refreshHomeView() {
        homeContainer.removeAll(); 
        homeContainer.add(createHomePanel(), BorderLayout.CENTER); 
        homeContainer.revalidate(); 
        homeContainer.repaint(); 
    }

    // ==========================================
    // 4. HOME & SCHEDULE PANELS
    // ==========================================
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        boolean hasEvents = adminDAO.hasSchedule(adminName);

        if (!hasEvents) {
            JPanel emptyStatePanel = new JPanel(new GridBagLayout());
            emptyStatePanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0; gbc.gridy = 0;

            JLabel noEventsLabel = new JLabel("You have no upcoming events.");
            noEventsLabel.setFont(new Font("Arial", Font.BOLD, 24));
            noEventsLabel.setForeground(Color.GRAY);
            emptyStatePanel.add(noEventsLabel, gbc);

            gbc.gridy = 1;
            JButton getStartedBtn = new JButton("Get started with your schedule");
            getStartedBtn.setFont(new Font("Arial", Font.BOLD, 16));
            getStartedBtn.setBackground(new Color(46, 204, 113)); 
            getStartedBtn.setForeground(Color.WHITE);
            getStartedBtn.setFocusPainted(false);
            getStartedBtn.setPreferredSize(new Dimension(300, 40));
            
            getStartedBtn.addActionListener(e -> cardLayout.show(dynamicContentPanel, "ScheduleForm"));
            emptyStatePanel.add(getStartedBtn, gbc);
            panel.add(emptyStatePanel, BorderLayout.CENTER);
            
        } else {
            JPanel scheduleWrapper = new JPanel(new BorderLayout(10, 10));
            scheduleWrapper.setBackground(Color.WHITE);
            scheduleWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Your Weekly Schedule");
            title.setFont(new Font("Arial", Font.BOLD, 22));
            title.setForeground(new Color(41, 128, 185));
            scheduleWrapper.add(title, BorderLayout.NORTH);

            String currentView = (String) globalDivisionToggle.getSelectedItem();
            scheduleTable = new JTable(adminDAO.getAdminSchedule(adminName, currentView));
            scheduleTable.setRowHeight(30); 
            scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            scheduleTable.getTableHeader().setBackground(new Color(236, 240, 241));
            
            scheduleWrapper.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

            JButton addMoreBtn = new JButton("+ Add Another Slot");
            addMoreBtn.setBackground(new Color(52, 152, 219));
            addMoreBtn.setForeground(Color.WHITE);
            addMoreBtn.setFocusPainted(false);
            addMoreBtn.addActionListener(e -> cardLayout.show(dynamicContentPanel, "ScheduleForm"));
            
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.add(addMoreBtn);
            scheduleWrapper.add(bottomPanel, BorderLayout.SOUTH);

            panel.add(scheduleWrapper, BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel createScheduleFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Add Weekly Lecture Slot");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        panel.add(new JLabel("Target Course:"), gbc);
        gbc.gridx = 1;
        String[] courses = {
            "BTech IT", "BTech AI", "BTech Computer", "BTech DS", 
            "MBA Tech IT", "MBA Tech AI", "MBA Tech Computer", "MBA Tech DS", "BTI"
        };
        JComboBox<String> courseBox = new JComboBox<>(courses);
        panel.add(courseBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        String[] semesters = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6", "Semester 7", "Semester 8"};
        JComboBox<String> semesterBox = new JComboBox<>(semesters);
        panel.add(semesterBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1;
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        JComboBox<String> dayBox = new JComboBox<>(days);
        panel.add(dayBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Start Time (e.g., 09:00 AM):"), gbc);
        gbc.gridx = 1;
        JTextField startTimeField = new JTextField(10);
        panel.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("End Time (e.g., 10:30 AM):"), gbc);
        gbc.gridx = 1;
        JTextField endTimeField = new JTextField(10);
        panel.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        JTextField subjectField = new JTextField(15);
        panel.add(subjectField, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Division:"), gbc);
        gbc.gridx = 1;
        String[] divs = {"Div A", "Div B", "Div C"};
        JComboBox<String> divBox = new JComboBox<>(divs);
        panel.add(divBox, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("Save Time Slot");
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            boolean success = adminDAO.addScheduleSlot(
                adminName,
                (String)courseBox.getSelectedItem(),
                (String)semesterBox.getSelectedItem(), 
                (String)dayBox.getSelectedItem(), 
                startTimeField.getText().trim(), 
                endTimeField.getText().trim(), 
                subjectField.getText().trim(), 
                (String)divBox.getSelectedItem()
            );

            if(success) {
                JOptionPane.showMessageDialog(panel, "Slot added successfully!");
                startTimeField.setText(""); endTimeField.setText(""); subjectField.setText("");
                courseBox.setSelectedIndex(0); semesterBox.setSelectedIndex(0); dayBox.setSelectedIndex(0); divBox.setSelectedIndex(0);
                
                refreshHomeView(); // Instantly update the table
                cardLayout.show(dynamicContentPanel, "Home");
            } else {
                JOptionPane.showMessageDialog(panel, "Error saving schedule.");
            }
        });

        return panel;
    }

    // ==========================================
    // 5. CONTEXTUAL STUDENTS WORKSPACE
    // ==========================================
   // ==========================================
    // 5. CONTEXTUAL STUDENTS WORKSPACE
    // ==========================================
    private JPanel createStudentsWorkspace() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- TOP CONTEXT BAR ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topBar.setBackground(new Color(236, 240, 241));
        topBar.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        JLabel filterLabel = new JLabel("Select Allocated Batch:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JComboBox<String> batchDropdown = new JComboBox<>();
        batchDropdown.setPreferredSize(new Dimension(350, 30));
        
        java.util.List<String> assignedBatches = adminDAO.getAssignedBatches(adminName);
        if (assignedBatches.isEmpty()) {
            batchDropdown.addItem("No batches assigned to you yet.");
            batchDropdown.setEnabled(false);
        } else {
            for (String batch : assignedBatches) {
                batchDropdown.addItem(batch);
            }
        }

        topBar.add(filterLabel);
        topBar.add(batchDropdown);
        panel.add(topBar, BorderLayout.NORTH);

        // --- CENTER TABLE AREA ---
        JTable studentTable = new JTable();
        studentTable.setRowHeight(28);
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(studentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM ACTION BAR (NEW BUTTON ADDED) ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(Color.WHITE);

        JButton viewAnalyticsBtn = new JButton("View Analytics");
        viewAnalyticsBtn.setBackground(new Color(52, 152, 219)); // Professional Blue
        viewAnalyticsBtn.setForeground(Color.WHITE);

        JButton editStudentBtn = new JButton("Edit Selected Student");
        editStudentBtn.setBackground(new Color(241, 196, 15));
        editStudentBtn.setForeground(Color.BLACK);

        JButton addStudentBtn = new JButton("+ Register Student to Batch");
        addStudentBtn.setBackground(new Color(46, 204, 113));
        addStudentBtn.setForeground(Color.WHITE);
        
        // Add them to the bar
        bottomBar.add(viewAnalyticsBtn);
        bottomBar.add(editStudentBtn);
        bottomBar.add(addStudentBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        // --- ACTION LISTENERS ---

        // 1. Dropdown Change Listener
        batchDropdown.addActionListener(e -> {
            if (!batchDropdown.isEnabled()) return;
            String selected = (String) batchDropdown.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split(" - ");
                if (parts.length == 3) {
                    DefaultTableModel model = studentDAO.getStudentsByBatch(parts[0], parts[1], parts[2]);
                    studentTable.setModel(model);
                }
            }
        });

        if (batchDropdown.isEnabled() && batchDropdown.getItemCount() > 0) {
            batchDropdown.setSelectedIndex(0); 
        }

        // 2. View Analytics Logic (Replaces Double-Click)
        viewAnalyticsBtn.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a student from the table first.", "Select a Student", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Grab the student's info from the highlighted row
            String rollNo = (String) studentTable.getValueAt(selectedRow, 0);
            String studentName = (String) studentTable.getValueAt(selectedRow, 1);
            
            // Extract the subject from the currently selected batch dropdown
            String currentBatch = (String) batchDropdown.getSelectedItem();
            String[] parts = currentBatch.split(" - ");
            String subject = adminDAO.getSubjectForBatch(adminName, parts[0], parts[1], parts[2]);
            
            // Trigger the chart rendering method
            showStudentProfile(rollNo, studentName, subject);
        });

        // 3. Add Student Logic
        addStudentBtn.addActionListener(e -> {
            if (!batchDropdown.isEnabled()) {
                JOptionPane.showMessageDialog(panel, "You must be assigned to a batch first!");
                return;
            }
            
            String currentBatch = (String) batchDropdown.getSelectedItem();
            String[] parts = currentBatch.split(" - ");
            String cCourse = parts[0]; String cSem = parts[1]; String cDiv = parts[2];

            JTextField rollField = new JTextField(10);
            JTextField nameField = new JTextField(15);
            JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2026, 2000, 2100, 1));
            yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));

            Object[] message = {
                "Registering to: " + currentBatch,
                "Roll Number:", rollField,
                "Full Name:", nameField,
                "Enrollment Year:", yearSpinner
            };

            int option = JOptionPane.showConfirmDialog(panel, message, "New Student Registration", JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                String newRoll = rollField.getText().trim();
                String newName = nameField.getText().trim();
                int newYear = (int) yearSpinner.getValue();

                if (newRoll.isEmpty() || newName.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Roll Number and Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean success = studentDAO.addStudent(newRoll, newName, cCourse, cSem, cDiv, newYear, adminName);
                
                if (success) {
                    JOptionPane.showMessageDialog(panel, newName + " successfully registered to " + cCourse + "!");
                    DefaultTableModel freshModel = studentDAO.getStudentsByBatch(cCourse, cSem, cDiv);
                    studentTable.setModel(freshModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Database error. Roll Number might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

   
   private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Post New Assignment");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        // --- DYNAMIC BATCH DROPDOWN ---
        gbc.gridwidth = 1; gbc.gridy = 1;
        panel.add(new JLabel("Target Batch:"), gbc);
        gbc.gridx = 1;
        
        JComboBox<String> batchDropdown = new JComboBox<>();
        // Fetch the exact same assigned batches used in the Students tab
        java.util.List<String> assignedBatches = adminDAO.getAssignedBatches(adminName);
        if (assignedBatches.isEmpty()) {
            batchDropdown.addItem("No batches assigned to your schedule.");
            batchDropdown.setEnabled(false);
        } else {
            for (String batch : assignedBatches) {
                batchDropdown.addItem(batch);
            }
        }
        panel.add(batchDropdown, gbc);

        // --- THE REST OF THE FORM ---
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Assignment Title:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField dateField = new JTextField(10);
        panel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTH;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        panel.add(scrollPane, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        JButton postBtn = new JButton("Post Assignment");
        postBtn.setBackground(new Color(46, 204, 113));
        postBtn.setForeground(Color.WHITE);
        panel.add(postBtn, gbc);

        // --- ACTION LISTENER ---
        postBtn.addActionListener(e -> {
            if (!batchDropdown.isEnabled()) {
                JOptionPane.showMessageDialog(panel, "You must be assigned to a batch to post assignments.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Extract Course, Semester, and Division from the single dropdown string
            String selectedBatch = (String) batchDropdown.getSelectedItem();
            String[] parts = selectedBatch.split(" - ");
            String cCourse = parts[0]; 
            String cSem = parts[1]; 
            String cDiv = parts[2];

            boolean success = adminDAO.postAssignment(
                cCourse, cSem, cDiv, // Passing the extracted batch details!
                titleField.getText().trim(), descArea.getText().trim(),
                dateField.getText().trim(), adminName
            );
            
            if(success) {
                JOptionPane.showMessageDialog(panel, "Assignment posted successfully to " + selectedBatch + "!");
                titleField.setText(""); dateField.setText(""); descArea.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to post assignment. Check date format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

   private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP CONTEXT & INPUT AREA ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Batch Selection
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Allocated Batch:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        JComboBox<String> batchBox = new JComboBox<>();
        List<String> batches = adminDAO.getAssignedBatches(adminName);
        for (String b : batches) batchBox.addItem(b);
        topPanel.add(batchBox, gbc);

        // Roll Number Selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        topPanel.add(new JLabel("Student Roll No:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        JComboBox<String> rollBox = new JComboBox<>();
        topPanel.add(rollBox, gbc);

        // Grade Inputs (Assuming each is out of 20 for a total of 100)
        gbc.gridwidth = 1; gbc.gridy = 2;
        gbc.gridx = 0; topPanel.add(new JLabel("MTT1 (20):"), gbc);
        gbc.gridx = 1; JTextField mtt1Field = new JTextField(5); topPanel.add(mtt1Field, gbc);
        
        gbc.gridx = 2; topPanel.add(new JLabel("MTT2 (20):"), gbc);
        gbc.gridx = 3; JTextField mtt2Field = new JTextField(5); topPanel.add(mtt2Field, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0; topPanel.add(new JLabel("Project (20):"), gbc);
        gbc.gridx = 1; JTextField projField = new JTextField(5); topPanel.add(projField, gbc);
        
        gbc.gridx = 2; topPanel.add(new JLabel("Lab (20):"), gbc);
        gbc.gridx = 3; JTextField labField = new JTextField(5); topPanel.add(labField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0; topPanel.add(new JLabel("Class Part. (20):"), gbc);
        gbc.gridx = 1; JTextField partField = new JTextField(5); topPanel.add(partField, gbc);

        // Generate Button
        gbc.gridx = 2; gbc.gridwidth = 2;
        JButton generateBtn = new JButton("Save Marks & Generate Report");
        generateBtn.setBackground(new Color(46, 204, 113));
        generateBtn.setForeground(Color.WHITE);
        topPanel.add(generateBtn, gbc);

        panel.add(topPanel, BorderLayout.NORTH);

        // --- REPORT OUTPUT AREA ---
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(253, 254, 254));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        // --- ACTION LISTENERS ---

        // Auto-update Roll Numbers when Batch changes
        batchBox.addActionListener(e -> {
            rollBox.removeAllItems();
            String selected = (String) batchBox.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                String[] parts = selected.split(" - ");
                List<String> rolls = studentDAO.getRollNumbersInBatch(parts[0], parts[1], parts[2]);
                for (String r : rolls) rollBox.addItem(r);
            }
        });
        
        // Trigger first load
        if (batchBox.getItemCount() > 0) batchBox.setSelectedIndex(0);

        generateBtn.addActionListener(e -> {
            String rollNo = (String) rollBox.getSelectedItem();
            if (rollNo == null) return;

            try {
                int mtt1 = Integer.parseInt(mtt1Field.getText().trim());
                int mtt2 = Integer.parseInt(mtt2Field.getText().trim());
                int proj = Integer.parseInt(projField.getText().trim());
                int lab = Integer.parseInt(labField.getText().trim());
                int part = Integer.parseInt(partField.getText().trim());

                // Find out what subject the admin teaches for this batch
                String selectedBatch = (String) batchBox.getSelectedItem();
                String[] parts = selectedBatch.split(" - ");
                String subject = adminDAO.getSubjectForBatch(adminName, parts[0], parts[1], parts[2]);

                // Save to Database
                boolean saved = studentDAO.saveDetailedMarks(rollNo, subject, mtt1, mtt2, proj, lab, part);
                
                if (saved) {
                    // Generate and show the report
                    String report = studentDAO.generateDetailedReportCard(rollNo);
                    reportArea.setText(report);
                    
                    // Clear inputs
                    mtt1Field.setText(""); mtt2Field.setText(""); projField.setText(""); 
                    labField.setText(""); partField.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to save marks.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter valid numbers for all marking fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    
private JPanel createQueriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Pending Student Queries");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(231, 76, 60)); // Alert Red
        panel.add(title, BorderLayout.NORTH);

        // --- FETCH PENDING QUERIES ---
        DefaultTableModel model = new DefaultTableModel(new String[]{"Query ID", "Roll No", "Question"}, 0);
        String sql = "SELECT query_id, roll_number, question FROM student_queries WHERE status = 'Pending'";
        try (java.sql.Connection conn = DBconnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("query_id"), rs.getString("roll_number"), rs.getString("question")});
            }
        } catch (Exception e) { e.printStackTrace(); }

        JTable queryTable = new JTable(model);
        queryTable.setRowHeight(30);
        panel.add(new JScrollPane(queryTable), BorderLayout.CENTER);

        // --- RESPONSE AREA ---
        JPanel responsePanel = new JPanel(new BorderLayout(5, 5));
        responsePanel.setBackground(Color.WHITE);
        responsePanel.add(new JLabel("Your Response:"), BorderLayout.NORTH);
        
        JTextArea responseArea = new JTextArea(4, 20);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send Response & Resolve");
        sendBtn.setBackground(new Color(46, 204, 113));
        sendBtn.setForeground(Color.WHITE);
        responsePanel.add(sendBtn, BorderLayout.EAST);

        panel.add(responsePanel, BorderLayout.SOUTH);

        // --- ACTION LISTENER ---
        sendBtn.addActionListener(e -> {
            int selectedRow = queryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a query from the table first.");
                return;
            }
            
            String response = responseArea.getText().trim();
            if (response.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Response cannot be empty.");
                return;
            }

            int queryId = (int) queryTable.getValueAt(selectedRow, 0);
            if (adminDAO.respondToQuery(queryId, response)) {
                JOptionPane.showMessageDialog(panel, "Response sent! Query resolved.");
                model.removeRow(selectedRow); // Remove it from the UI immediately
                responseArea.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "Database error.");
            }
        });

        return panel;
    }

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard("Admin_Test").setVisible(true));
    }

    private void showStudentProfile(String rollNo, String studentName, String subject) {
        JDialog profileDialog = new JDialog(this, "Student Profile: " + studentName, true);
        profileDialog.setSize(800, 500);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new GridLayout(1, 2, 10, 10)); // Split screen horizontally
        
  
        int[] attendance = studentDAO.getAttendanceStats(rollNo, subject);
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        pieDataset.setValue("Present", attendance[0]);
        pieDataset.setValue("Absent/Missed", attendance[1]);
        
        JFreeChart pieChart = ChartFactory.createPieChart(
            "Attendance: " + subject,
            pieDataset,
            true, true, false
        );
        ChartPanel piePanel = new ChartPanel(pieChart);
        profileDialog.add(piePanel);

        
        int[] assignments = studentDAO.getAssignmentStats(rollNo);
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        barDataset.addValue(assignments[0], "Count", "Accepted");
        barDataset.addValue(assignments[1], "Count", "Pending");
        barDataset.addValue(assignments[2], "Count", "Rejected");
        
        JFreeChart barChart = ChartFactory.createBarChart(
            "Assignment Status",
            "Status", "Number of Assignments",
            barDataset,
            PlotOrientation.VERTICAL,
            false, true, false
        );
        ChartPanel barPanel = new ChartPanel(barChart);
        profileDialog.add(barPanel);

        profileDialog.setVisible(true);
    }
}