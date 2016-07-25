package model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import model.dao.databases.Stored;
import model.dao.databases.StoredField;
import org.jetbrains.annotations.Nullable;

import static com.aldor.utils.CryptoUtils.stringRandomSaltedHash;
import static com.aldor.utils.CryptoUtils.verifySaltedHash;

/**
 * Username and password
 */
@AllArgsConstructor
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

    /**
     * If {@code saltedHash == true}, applies hashing to password. Otherwise, does nothing.
     * Call this method after creating {@code Credentials} object with a constructor.
     * Multiple calls will rehash the password and make it unusable!
     * @return Reference to this object
     */
    public Credentials applyHash() {
        if (pwd == null) pwd = "";
        if (saltedHash) pwd = stringRandomSaltedHash(pwd);
        return this;
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
