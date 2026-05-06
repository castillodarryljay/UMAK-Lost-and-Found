/**
 * Session management class storing current user information.
 * Provides static access to user details across the application.
 *
 * @author UMAK Lost & Found Team
 */
public class UserSession {
    public static int userId;
    public static String fullName;
    public static String role;
    public static String studentNo;
    public static String email;
    public static String department;

    public static void init(int id, String name, String r, String sno, String mail, String dept) {
        userId = id;
        fullName = name;
        role = r;
        studentNo = sno;
        email = mail;
        department = dept;
    }
}