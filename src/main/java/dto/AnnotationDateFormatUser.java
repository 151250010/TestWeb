package dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

public class AnnotationDateFormatUser {

    private String userName;
    private String phoneNumber;
    private boolean isVip;

    @DateTimeFormat(pattern = "yyyy-MM-dd") //可以将前端传来的日期进行格式化
    private Date birthday;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }
}
