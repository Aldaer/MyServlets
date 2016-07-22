package model.dao;

import com.aldor.utils.CryptoUtils;
import lombok.Getter;
import model.dao.common.Stored;
import model.dao.common.StoredField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.aldor.utils.CryptoUtils.verifySaltedHash;

/**
 * Username and password
 */
public class Credentials implements Stored {
    @Getter
    @StoredField(column = "username", maxLength = 50)
    private String uName;
    @StoredField(column="dpassword", maxLength = 80)
    private String pwd;

    private final boolean saltedHash;

    public Credentials(boolean saltedHash) {
        this("", "", saltedHash);
    }

    public Credentials(@NotNull String uName, @NotNull String pwd, boolean saltedHash) {
        this.uName = uName;
        this.saltedHash = saltedHash;
        this.pwd = saltedHash? CryptoUtils.stringRandomSaltedHash(pwd) : pwd;
    }

    /**
     * Do username and password match?
     * @param password Password presented by user (always plain-text)
     * @return True if password matches the credentials
     */
    public boolean verify(@Nullable String password) {
        if (saltedHash) return verifySaltedHash(pwd, password);
        else return (pwd != null && pwd.equals(password));
    }
}
