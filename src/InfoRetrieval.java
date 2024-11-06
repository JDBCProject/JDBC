import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.sql.DriverManager.getConnection;

public class InfoRetrieval extends JFrame implements ActionListener {
    private static final String url = "jdbc:mysql://localhost:3306/mydb?serverTimeZone=UTC";
    private static final String usr = "root";
    private static final String password = "12345678";

    private static final String[] departmentContent = {"Research", "Administration", "Headquarters"}; // 부서 카테고리
    private static final String[] sexContent = {"F", "M"}; // 성별 카테고리
    private JComboBox<String> CategoryBox;
    private JComboBox<String> ConditionBox;
    private JTextField salaryTextField; // 연봉 입력 필드
    private JTextArea selectedEmpInfo = new JTextArea(2, 80); // 선택한 직원의 이름을 가져오는 필드
    private JTextArea totalEmpAmt = new JTextArea(1, 10); // 선택한 직원의 수

    private JLabel timeLabel = new JLabel(); // 현재 시간 표시
    private JLabel selectedEmp = new JLabel("선택한 직원: ");
    private JLabel totalEmptxt = new JLabel("선택한 인원 수 : ");
    private JButton RetrievalBtn = new JButton("직원 검색"); // 정보 검색 버튼
    private JButton DeleteInfoBtn = new JButton("데이터 삭제"); // 정보 제거 버튼


    private JCheckBox name = new JCheckBox("Name(이름)", true);
    private JCheckBox ssn = new JCheckBox("Ssn(주민번호)", true);
    private JCheckBox bdate = new JCheckBox("Bdate(생일)", true);
    private JCheckBox address = new JCheckBox("Address(주소)", true);
    private JCheckBox sex = new JCheckBox("Sex(성별)", true);
    private JCheckBox salary = new JCheckBox("Salary(연봉)", true);
    private JCheckBox supervisor = new JCheckBox("Supervisor(상사)", true);
    private JCheckBox department = new JCheckBox("Department(부서)", true);

    public InfoRetrieval() {
        selectedEmpInfo.setEditable(false);
        selectedEmpInfo.setLineWrap(true);
        selectedEmpInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        totalEmpAmt.setEditable(false);
        totalEmpAmt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLook();
        BasicUI();

        setTitle("Company Employee Retrieval System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 700);
        setLocationRelativeTo(null);
        setVisible(true);

        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
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
        JPanel jPanel0 = new JPanel();
        jPanel0.setLayout(new FlowLayout(FlowLayout.LEFT));
        jPanel0.setBackground(new Color(230, 230, 250));
        jPanel0.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel rangeLabel = new JLabel("검색 범위 ");
        rangeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        jPanel0.add(rangeLabel);

        String[] categorybox = {"전체", "부서", "성별", "연봉"};
        CategoryBox = new JComboBox<>(categorybox);
        CategoryBox.addActionListener(this);
        ConditionBox = new JComboBox<>();
        salaryTextField = new JTextField(10);
        salaryTextField.setVisible(false);

        jPanel0.add(CategoryBox);
        jPanel0.add(ConditionBox);
        jPanel0.add(salaryTextField);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(new Color(230, 230, 250));
        timePanel.add(timeLabel);
        jPanel0.add(timePanel);

        JPanel ContentCheckPanel = new JPanel();
        ContentCheckPanel.setLayout(new BoxLayout(ContentCheckPanel, BoxLayout.Y_AXIS));
        ContentCheckPanel.setBackground(new Color(230, 230, 250));
        ContentCheckPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("검색 항목"),
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

        JPanel TopPanel = new JPanel(new BorderLayout());
        TopPanel.setBackground(new Color(230, 230, 250));
        TopPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        TopPanel.add(jPanel0, BorderLayout.NORTH);
        TopPanel.add(ContentCheckPanel, BorderLayout.CENTER);

        JPanel SelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        SelectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        SelectPanel.setBackground(new Color(230, 230, 250));
        selectedEmp.setFont(new Font("Arial", Font.BOLD, 14));
        SelectPanel.add(selectedEmp);
        SelectPanel.add(selectedEmpInfo);

        JPanel TotalEmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        TotalEmpPanel.add(totalEmptxt);
        TotalEmpPanel.setBackground(new Color(230, 230, 250));
        TotalEmpPanel.setFont(new Font("Arial", Font.BOLD, 14));
        totalEmptxt.setFont(new Font("Arial", Font.BOLD,14));
        TotalEmpPanel.add(totalEmpAmt);

        JPanel DeleteInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        DeleteInfoPanel.setBackground(new Color(230, 230, 250));
        DeleteInfoPanel.add(DeleteInfoBtn);

        JPanel BtmPanel = new JPanel();
        BtmPanel.setBackground(new Color(230, 230, 250));
        BtmPanel.setLayout(new BoxLayout(BtmPanel, BoxLayout.X_AXIS));
        BtmPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        BtmPanel.add(TotalEmpPanel);
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
                 BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }


    private void ConnectToMySQL() { // MySQL 연결
        try {
            Connection conn = DriverManager.getConnection(url, usr, password);
            Statement stmt = conn.createStatement();

            System.out.println("데이터베이스 연결 성공!");
        } catch (SQLException e) {
            System.out.println("데이터베이스 연결 실패.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConnectToMySQL();
        if(e.getSource() == CategoryBox) {
            String selectedCategory = (String)CategoryBox.getSelectedItem();
            ConditionBox.setVisible(false);
            salaryTextField.setVisible(false);

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
        // 검색, 제거, 수정 알고리즘 수행
        if(e.getSource() == RetrievalBtn) {
            performSearchInfo();
        }
        else if(e.getSource() == DeleteInfoBtn) {
            performDeleteInfo();
        }
//        else if(e.getSource() == UpdateInfoBtn) {
//            performUpdateInfo();
//        }
    }
    private void performSearchInfo() {
        // 직원 정보 검색 알고리즘
    }
    private void performDeleteInfo() {
        // 직원 정보 제거 알고리즘
    }
    private void performUpdateInfo() {
        // 직원 정보 수정 알고리즘
    }
    public static void main(String[] args) {
        new InfoRetrieval();
    }
}
