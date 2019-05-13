package learn.hugy.com.pushsocket.model;

/**
 * @author hugy
 */
public class IdBean {
    /**
     * 唯一身份标识
     */
    private String idNum;
    /**
     * 用户分组，如有权限控制，应该根据权限来
     */
    private String group;
    /**
     * 用户账户
     */
    private String userAccount;
    /**
     * 用户手机号
     */
    private String phone;

    public IdBean() {
    }

    public IdBean(String idNum, String group, String userAccount, String phone) {
        this.idNum = idNum;
        this.group = group;
        this.userAccount = userAccount;
        this.phone = phone;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return idNum + '☆'
                + group + '☆'
                + userAccount + '☆'
                + phone + '☆';
    }
}
