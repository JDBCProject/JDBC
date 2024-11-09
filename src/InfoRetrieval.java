import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.sql.DriverManager.getConnection;

public class InfoRetrieval extends JFrame implements ActionListener {

    private Connection conn;

    private static final String[] departmentContent = {"Research", "Administration", "Headquarters"}; // 부서 카테고리
    private static final String[] sexContent = {"F", "M"}; // 성별 카테고리
    private JComboBox<String> CategoryBox;
    private JComboBox<String> ConditionBox;
    private JComboBox<String> AvgSalCategoryBox;
    private JTextField salaryTextField; // 연봉 입력 필드
    private JTextArea selectedEmpInfo = new JTextArea(2, 80); // 선택한 직원의 이름을 가져오는 필드

    private JLabel timeLabel = new JLabel(); // 현재 시간 표시
    private JLabel avgSalaryLabel;
    private JTable showEmpTable;
    private DefaultTableModel defaultTableModel;
    private JLabel selectedEmp = new JLabel("선택한 직원 이름: ");
    private JButton RetrievalBtn = new JButton("직원 검색"); // 정보 검색 버튼
    private JButton DeleteInfoBtn = new JButton("데이터 삭제"); // 정보 제거 버튼
    private JButton AddEmpInfoBtn = new JButton("직원 추가하기");


    private JCheckBox name = new JCheckBox("Name(이름)", true);
    private JCheckBox ssn = new JCheckBox("Ssn(주민번호)", true);
    private JCheckBox bdate = new JCheckBox("Bdate(생일)", true);
    private JCheckBox address = new JCheckBox("Address(주소)", true);
    private JCheckBox sex = new JCheckBox("Sex(성별)", true);
    private JCheckBox salary = new JCheckBox("Salary(연봉)", true);
    private JCheckBox supervisor = new JCheckBox("Supervisor(상사)", true);
    private JCheckBox department = new JCheckBox("Department(부서)", true);

    public InfoRetrieval(Connection connection) {
        this.conn = connection;
        if (conn != null) {
            System.out.println("데이터베이스 연결 성공!");
        }

        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();

        selectedEmpInfo.setEditable(false);
        selectedEmpInfo.setLineWrap(true);
        selectedEmpInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        setLook();
        BasicUI();

        setTitle("Company Employee Retrieval System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 700);
        setLocationRelativeTo(null);
        setVisible(true);

        performSearchInfo();
    }
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        timeLabel.setText("현재 시간 : " + currentTime);
    }
    private void setLook() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("오류 발생" + e.getMessage());
        }
    }
    private void BasicUI() {

        defaultTableModel = new DefaultTableModel();
        showEmpTable = new JTable(defaultTableModel);
        JScrollPane tableScroll = new JScrollPane(showEmpTable);

        String[] columns = {"Name", "Ssn", "Bdate", "Address", "Sex", "Salary", "Super_ssn", "Department_Name"};
        for(String column : columns) {
            defaultTableModel.addColumn(column);
        }

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Employee Information",
                        SwingConstants.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), Color.WHITE),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tablePanel.setBackground(Color.BLACK);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timePanel.setBackground(new Color(230, 230, 250));
        timePanel.setFont(new Font("Arial", Font.BOLD, 16));
        timePanel.add(timeLabel);

        JPanel jPanel0 = new JPanel();
        jPanel0.setLayout(new FlowLayout(FlowLayout.LEFT));
        jPanel0.setBackground(new Color(230, 230, 250));
        jPanel0.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel rangeLabel = new JLabel("검색 범위 ");
        rangeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rangeLabel.setForeground(Color.DARK_GRAY);
        jPanel0.add(rangeLabel);

        String[] categorybox = {"전체", "부서", "성별", "연봉"};
        CategoryBox = new JComboBox<>(categorybox);
        CategoryBox.addActionListener(this);
        CategoryBox.setFont(new Font("Arial", Font.PLAIN, 14));
        CategoryBox.setBackground(Color.WHITE);
        ConditionBox = new JComboBox<>();
        salaryTextField = new JTextField(10);
        salaryTextField.setVisible(false);

        jPanel0.add(CategoryBox);
        jPanel0.add(ConditionBox);
        jPanel0.add(salaryTextField);

        avgSalaryLabel = new JLabel("평균 급여 기준: ");
        avgSalaryLabel.setVisible(false);
        avgSalaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        avgSalaryLabel.setForeground(Color.DARK_GRAY);
        jPanel0.add(avgSalaryLabel);

        String[] avgSalCategory = {"그룹 없음", "성별", "부서", "상급자"};
        AvgSalCategoryBox = new JComboBox<>(avgSalCategory);
        AvgSalCategoryBox.addActionListener(this);
        AvgSalCategoryBox.setFont(new Font("Arial", Font.PLAIN, 14));
        AvgSalCategoryBox.setBackground(Color.WHITE);
        AvgSalCategoryBox.setVisible(false);
        jPanel0.add(AvgSalCategoryBox);

        AddEmpInfoBtn.setFont(new Font("Arial", Font.BOLD, 12));
        AddEmpInfoBtn.setBackground(new Color(70, 130, 180));
        AddEmpInfoBtn.setForeground(Color.WHITE);
        AddEmpInfoBtn.addActionListener(e -> openAddEmpWindow());
        jPanel0.add(AddEmpInfoBtn);

        JPanel ContentCheckPanel = new JPanel();
        ContentCheckPanel.setLayout(new BoxLayout(ContentCheckPanel, BoxLayout.Y_AXIS));
        ContentCheckPanel.setBackground(new Color(230, 230, 250));
        ContentCheckPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "검색 항목",
                        SwingConstants.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        Font ContentCheckPanelFont = new Font("Arial", Font.PLAIN, 14);
        JCheckBox[] checkBoxes = {name, ssn, bdate, address, sex, salary, supervisor, department};
        for (JCheckBox jcheckBox : checkBoxes) {
            jcheckBox.setFont(ContentCheckPanelFont);
            jcheckBox.setBackground(new Color(230, 230, 250));
            ContentCheckPanel.add(jcheckBox);
        }

        ContentCheckPanel.add(Box.createVerticalStrut(10));
        ContentCheckPanel.add(RetrievalBtn);

        BtnUI(RetrievalBtn, new Color(70, 130, 180));
        BtnUI(DeleteInfoBtn, new Color(70, 130, 180));
        BtnUI(AddEmpInfoBtn, new Color(70, 130, 180));

        JPanel TopPanel = new JPanel(new BorderLayout());
        TopPanel.setBackground(new Color(230, 230, 250));
        TopPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        TopPanel.add(timePanel, BorderLayout.NORTH);
        TopPanel.add(jPanel0, BorderLayout.CENTER);
        TopPanel.add(ContentCheckPanel, BorderLayout.SOUTH);

        JPanel SelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        SelectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        SelectPanel.setBackground(new Color(230, 230, 250));
        selectedEmp.setFont(new Font("Arial", Font.BOLD, 14));
        SelectPanel.add(selectedEmp);
        SelectPanel.add(selectedEmpInfo);


        JPanel DeleteInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        DeleteInfoPanel.setBackground(new Color(230, 230, 250));
        DeleteInfoPanel.add(DeleteInfoBtn);

        JPanel BtmPanel = new JPanel();
        BtmPanel.setBackground(new Color(230, 230, 250));
        BtmPanel.setLayout(new BoxLayout(BtmPanel, BoxLayout.X_AXIS));
        BtmPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        BtmPanel.add(DeleteInfoPanel);

        JPanel VerticalPanel = new JPanel();
        VerticalPanel.setBackground(new Color(230, 230, 250));
        VerticalPanel.setLayout(new BoxLayout(VerticalPanel, BoxLayout.Y_AXIS));
        VerticalPanel.add(SelectPanel);
        VerticalPanel.add(BtmPanel);

        add(VerticalPanel, BorderLayout.NORTH);
        add(TopPanel, BorderLayout.WEST);
    }
    // 버튼의 UI 결정 함수
    private void BtnUI(JButton jButton, Color backColor) {
        jButton.setBackground(backColor);
        jButton.setForeground(Color.BLACK);
        jButton.setFocusPainted(false);
        jButton.setFont(new Font("Arial", Font.BOLD, 12));
        jButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 50, 50), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }
//    private Connection ConnectToMySQL() { // MySQL 연결
//        try {
//            Connection conn = DriverManager.getConnection(url, usr, password);
//            return conn;
//        } catch (SQLException e) {
//            System.out.println("데이터베이스 연결 실패." + e.getMessage());
//            return null;
//        }
//    }
    private void openAddEmpWindow() { // 직원 정보 추가하는 새로운 창
        JFrame addEmployeeFrame = new JFrame("새로운 직원 정보 추가하기");
        addEmployeeFrame.setSize(450, 450);
        addEmployeeFrame.setLocationRelativeTo(this);

        String[] labels = {"Fname:", "Minit:", "Lname:", "Ssn:", "Bdate:", "Address:", "Sex:", "Salary:", "Super_ssn:", "Dno:"};
        JComponent[] attributes = new JComponent[labels.length];
        for(int i = 0; i < attributes.length; i++) {
            attributes[i] = new JTextField();
            if(i == 6) attributes[i] = new JComboBox<>(new String[] {"F", "M"});

        }
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(attributes[i]);
        }
        JButton addButton = new JButton("직원 정보 추가하기");
        addButton.addActionListener(e -> {
            try {
                performInsertInfo(attributes);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }); // 직원추가 호출
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);

        addEmployeeFrame.add(panel, BorderLayout.CENTER);
        addEmployeeFrame.add(buttonPanel, BorderLayout.SOUTH);

        addEmployeeFrame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == CategoryBox) {
            String selectedCategory = (String)CategoryBox.getSelectedItem();
            ConditionBox.setVisible(false);
            salaryTextField.setVisible(false);
            avgSalaryLabel.setVisible("전체".equals(selectedCategory));
            AvgSalCategoryBox.setVisible("전체".equals(selectedCategory));
            if("부서".equals(selectedCategory)) {
                ConditionBox.removeAllItems();
                for(String dept : departmentContent) {
                    ConditionBox.addItem((dept));
                }
                ConditionBox.setVisible(true);
            }
            else if("성별".equals(selectedCategory)) {
                ConditionBox.removeAllItems();
                for(String gender : sexContent) {
                    ConditionBox.addItem(gender);
                }
                ConditionBox.setVisible(true);
            }
            else if ("연봉".equals(selectedCategory)) {
                salaryTextField.setVisible(true);
            }
            else ConditionBox.addItem("없음");
        }
        // 검색, 제거 알고리즘 수행
        if(e.getSource() == RetrievalBtn) {
            performSearchInfo();
        }
        else if(e.getSource() == DeleteInfoBtn) {
            performDeleteInfo();
        }
    }
    private void performSearchInfo() {
        // 직원 정보 출력 알고리즘
        try {
            String querySearch = """
                SELECT CONCAT(Fname, ' ', Minit, ' ', Lname) AS Name,
                       Ssn, Bdate, Address, Sex, Salary, Super_ssn,
                       DEPARTMENT.Dname AS Department
                FROM EMPLOYEE JOIN DEPARTMENT ON EMPLOYEE.Dno = DEPARTMENT.Dnumber
                """;
            // 조인 해서 Department_name을 가져옴
            PreparedStatement statement = conn.prepareStatement(querySearch);
            ResultSet resultSet = statement.executeQuery();

            defaultTableModel.setRowCount(0);

            while (resultSet.next()) {
                Object[] row = {
                        resultSet.getString("Name"),
                        resultSet.getString("Ssn"),
                        resultSet.getString("Bdate"),
                        resultSet.getString("Address"),
                        resultSet.getString("Sex"),
                        resultSet.getString("Salary"),
                        resultSet.getString("Super_ssn"),
                        resultSet.getString("Department")
                };
                defaultTableModel.addRow(row);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void performDeleteInfo() {
        // 직원 정보 제거 알고리즘
    }
    private void performInsertInfo(JComponent[] attributes) throws SQLException {
        // 직원 정보 추가 알고리즘
        String fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno;

        String stmt = "INSERT INTO EMPLOYEE (Fname, Minit, Lname, Ssn, Bdate, Address, Sex, Salary, Super_ssn, Dno) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement p = conn.prepareStatement(stmt);

        fname = ((JTextField) attributes[0]).getText();
        minit = ((JTextField) attributes[1]).getText();
        lname = ((JTextField) attributes[2]).getText();
        ssn = ((JTextField) attributes[3]).getText();
        bdate = ((JTextField) attributes[4]).getText();
        address = ((JTextField) attributes[5]).getText();
        sex = (String) ((JComboBox<?>) attributes[6]).getSelectedItem();
        salary = ((JTextField) attributes[7]).getText();
        super_ssn = ((JTextField) attributes[8]).getText();
        dno = ((JTextField) attributes[9]).getText();

        p.clearParameters();
        p.setString(1, fname);
        p.setString(2, minit);
        p.setString(3, lname);
        p.setString(4, ssn);
        p.setString(5, bdate);
        p.setString(6, address);
        p.setString(7, sex);
        p.setString(8, salary);
        p.setString(9, super_ssn);
        p.setString(10, dno);

        // 값을 저장할 필요가 없으므로 바로 update
        p.executeUpdate();

        JOptionPane.showMessageDialog(null, "직원이 추가되었습니다."); // 추가 성공 메시지
        
        // 입력 필드 초기화 과정
        for (JComponent attribute : attributes) {
            if (attribute instanceof JTextField) ((JTextField) attribute).setText("");
            else ((JComboBox<?>) attribute).setSelectedIndex(0);

        }
        try {
            if(conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}