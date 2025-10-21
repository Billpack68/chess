package dataaccess;

import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class MemoryUserDAO {
    private Set<UserData> userData = new HashSet<>();
}
