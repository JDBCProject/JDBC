import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.List;

public class InfoRetrieval extends JFrame implements ActionListener {

    private Connection conn;

    private static final String[] departmentContent = {"Research", "Administration", "Headquarters"}; // 부서 카테고리
    private static final String[] sexContent = {"F", "M"}; // 성별 카테고리
    private JComboBox<String> CategoryBox;
    private JComboBox<String> ConditionBox;
    private JComboBox<String> AvgSalCategoryBox;
    private JTextField salaryTextField; // 연봉 입력 필드

    private JLabel timeLabel = new JLabel(); // 현재 시간 표시
    private JLabel avgSalaryLabel;
    private JTable showEmpTable;
    private DefaultTableModel defaultTableModel;
    private JButton RetrievalBtn = new JButton("직원 검색"); // 정보 검색 버튼
    private JButton DeleteInfoBtn = new JButton("직원 정보 삭제"); // 정보 제거 버튼
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
        AvgSalCategoryBox.setVisible(true);
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
        RetrievalBtn.addActionListener(this); // 검색 버튼 작동

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


        JPanel DeleteInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        DeleteInfoPanel.setBackground(new Color(230, 230, 250));
        DeleteInfoBtn.addActionListener(this);
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
                addEmployeeFrame.dispose();
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
            AvgSalCategoryBox.setSelectedItem("그룹 없음");
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
        // 리스트와 맵을 통해 체크박스와 컬럼 이름을 매핑 시킴
        try {
            List<Map.Entry<JCheckBox, String>> columnList = List.of(
                    Map.entry(name, "CONCAT(Fname,' ', Minit, ' ', Lname) AS Name"),
                    Map.entry(ssn, "Ssn"),
                    Map.entry(bdate, "Bdate"),
                    Map.entry(address, "Address"),
                    Map.entry(sex, "Sex"),
                    Map.entry(salary, "Salary"),
                    Map.entry(supervisor, "Super_ssn"),
                    Map.entry(department, "DEPARTMENT.Dname AS Department")
            );
            // 조건을 두어 각각 평균 연봉 검색
            String groupCategory = (String)AvgSalCategoryBox.getSelectedItem();
            if (!"그룹 없음".equals(groupCategory)) {
                String groupQuery = "";
                // 선택된 카테고리에 따라 다른 질의 형식
                switch (groupCategory) {
                    case "성별":
                        groupQuery = "SELECT Sex, AVG(Salary) AS Average_Salary FROM EMPLOYEE GROUP BY Sex";
                        break;
                    case "부서":
                        groupQuery = "SELECT DEPARTMENT.Dname AS Department_name, AVG(Salary) AS Average_Salary " +
                                "FROM EMPLOYEE JOIN DEPARTMENT ON EMPLOYEE.Dno = DEPARTMENT.Dnumber " +
                                "GROUP BY DEPARTMENT.Dname";
                        break;
                    case "상급자":
                        groupQuery = "SELECT EMPLOYEE.Super_ssn AS Supervisor, AVG(Salary) AS Average_Salary " +
                                "FROM EMPLOYEE GROUP BY EMPLOYEE.Super_ssn";
                        break;
                }
            if(!groupCategory.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(groupQuery);
                     ResultSet resultSet = stmt.executeQuery()) {

                    // 테이블 초기화 및 컬럼 재구성
                    defaultTableModel.setRowCount(0);
                    defaultTableModel.setColumnCount(0);

                    String[] columns;
                    // 카테고리 종류 마다 다른 컬럼 배열 구성
                    switch (groupCategory) {
                        case "성별":
                            columns = new String[]{"Sex", "Average_Salary"};
                            break;
                        case "부서":
                            columns = new String[]{"Department_name", "Average_Salary"};
                            break;
                        case "상급자":
                            columns = new String[]{"Supervisor", "Average_Salary"};
                            break;
                        default:
                            columns = new String[]{};
                    }
                    // 컬럼 이름 추가
                    for (String column : columns) {
                        defaultTableModel.addColumn(column);
                    }
                    // 행 데이터 추가
                    while (resultSet.next()) {
                        Object[] row = new Object[columns.length];
                        for (int i = 0; i < columns.length; i++) {
                            row[i] = resultSet.getObject(columns[i]);
                        }
                        defaultTableModel.addRow(row);
                    }
                }
                return;
                }
            }
            // 조건 검색(부서, 성별, 연봉)
            // 체크박스 선택된 컬럼만 질의 생성
            String columns = columnList.stream()
                    .filter(entry -> entry.getKey().isSelected())
                    .map(Map.Entry::getValue)                     // SQL 컬럼 이름 가져옴
                    .reduce((col1, col2) -> col1 + ", " + col2)
                    .orElse("*");

            String selectedCategory = (String)CategoryBox.getSelectedItem();
            String filterCondition = ""; // 추가할 조건 질의
            // 카테고리에서 선택된 이름에 따른 검색 조건 추가
            if("부서".equals(selectedCategory)) {
                filterCondition = "WHERE DEPARTMENT.Dname = ?";
            }
            else if("성별".equals(selectedCategory)) {
                filterCondition = "WHERE EMPLOYEE.Sex = ?";
            }
            else if("연봉".equals(selectedCategory)) {
                filterCondition = "WHERE EMPLOYEE.Salary >= ?";
            }
            // SQL 쿼리 생성
            String querySearch = "SELECT " + columns + " FROM EMPLOYEE JOIN DEPARTMENT ON EMPLOYEE.Dno = DEPARTMENT.Dnumber " + filterCondition;

            PreparedStatement stmt = conn.prepareStatement(querySearch);
            // 조건에 따른 값 설정
            if("부서".equals(selectedCategory)) {
                stmt.setString(1, (String)ConditionBox.getSelectedItem());
            }
            else if("성별".equals(selectedCategory)) {
                stmt.setString(1, (String)ConditionBox.getSelectedItem());
            }
            else if("연봉".equals(selectedCategory)) {
                stmt.setInt(1, Integer.parseInt(salaryTextField.getText()));
            }
            // 질의 실행
            ResultSet resultSet = stmt.executeQuery();

            // 테이블 초기화 및 컬럼 재구성
            defaultTableModel.setRowCount(0);
            defaultTableModel.setColumnCount(0);

            // 선택한 체크박스의 컬럼 추가
            columnList.stream()
                    .filter(entry -> entry.getKey().isSelected()) // 선택된 체크박스만 필터링
                    .map(entry -> entry.getValue().split(" AS ").length > 1 ? entry.getValue().split(" AS ")[1] : entry.getValue())
                    .forEach(defaultTableModel::addColumn);   // 테이블에 컬럼 추가
            // 선택한 체크박스의 데이터 행 추가
            while (resultSet.next()) {
                List<Object> row = new ArrayList<>();
                for (Map.Entry<JCheckBox, String> entry : columnList) {
                    if (entry.getKey().isSelected()) {
                        String columnAlias = entry.getValue().split(" AS ").length > 1 ? entry.getValue().split(" AS ")[1] : entry.getValue();
                        row.add(resultSet.getString(columnAlias));
                    }
                }
                defaultTableModel.addRow(row.toArray());
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void performDeleteInfo() {
        // 직원 정보 제거 알고리즘
        // 해당 코드 추가전에  git pull 만 하고 난 후,  실행 시 화면이 제대로 뜨지 않아 바로 추가하면 안 될 것 같아서 아래 코드는 주석처리로 해놓앗습니다.
            // 선택된 행
            int selectedRow = showEmpTable.getSelectedRow();
            //선택된 행이 없는 경우
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "삭제할 직원을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 선택된 직원의 SSN 갖고오기
            String selectedSsn = (String) defaultTableModel.getValueAt(selectedRow, 1); // ssn이 두번째 열이기에 1로 설정

            // DB에서 선택된 직원의 SSN을 이용하여 삭제하기
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM EMPLOYEE WHERE Ssn = ?")) {
                stmt.setString(1, selectedSsn);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "직원 정보가 삭제되었습니다."); //삭제 성공 메시지
                    // 보여지는 부분에서도 삭제하기
                    defaultTableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(this, "삭제할 직원을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
            // 오류가 있을 경우
            catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "데이터베이스 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }


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
        performSearchInfo(); // 정보 실시간 반영
        // 입력 필드 초기화 과정
        for (JComponent attribute : attributes) {
            if (attribute instanceof JTextField) ((JTextField) attribute).setText("");
            else ((JComboBox<?>) attribute).setSelectedIndex(0);

        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}