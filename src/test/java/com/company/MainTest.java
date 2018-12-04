package com.company;

import com.company.models.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {

    @Test
    public void testList() {
        List<String> names = new ArrayList<>();

        names.add("Jack");
        assertTrue(names.size() == 1);
    }

    @Test
    public void testUser() {
        String firstName = "Tom";
        String lastName = "Swift";

        User user = new User(2L, firstName, lastName);

        assertEquals(firstName, user.getFirstName());
    }

    @Test
    public void testMain() {
        Main main = new Main();

        User user1 = new User(2L, "Tom", "Swift");
        User user2 = new User(3L, "Jack", "Underhill");
        User user3 = new User(4L, "Susan", "James");
        User user4 = new User(5L, "Helen", "Smith");

        main.addUser(user1);
        main.addUser(user2);
        main.addUser(user3);
        main.addUser(user4);

        List<User> users = main.getUsers();

        assertTrue(users.size() == 4);
    }

    @Test
    public void testFull() {
        Main main = new Main();

        User user1 = new User(2L, "Tom", "Swift");
        User user2 = new User(3L, "Jack", "Underhill");

        main.addUser(user1);
        main.addUser(user2);
        main.removeUser(user1);

        List<User> users = main.getUsers();

        assertTrue(users.size() == 1);
    }

}
