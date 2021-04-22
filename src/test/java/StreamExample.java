import ch.qos.logback.core.boolex.EvaluationException;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import org.junit.jupiter.api.Test;

import javax.jws.soap.SOAPBinding;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class StreamExample {

    enum Role {
        ADMIN, USER, GUEST
    }

    static class User {

        public User(long id, String name, Role role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }

        private long id;
        private String name;
        private Role role;



        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Role getRole() {
            return role;
        }

    }

    @Test
    public void streamTestDemoJava7() {
        Collection<User> users = Arrays.asList(
                new User(1,"Tema",Role.USER),
                new User(12,"Vasya Pupkin",Role.ADMIN),
                new User(133,"Super cat",Role.GUEST),
                new User(11,"Super woman", Role.GUEST),
                new User(94,"Super man",Role.GUEST)
        );

        //Так фильтруем данные.
        List<User> usersFiltred = new LinkedList<>();

        for (User user: users) {
            if(user.getRole() == Role.GUEST) {
                usersFiltred.add(user);
            }
        }

        //Так сортируем данные.
        Comparator<User> comparator = (u1,u2) -> Long.compare(u1.getId(),u2.getId());

        Collections.sort(usersFiltred, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Long.compare(o1.getId(), o2.getId());
            }
        });

        //Сортировать можно так, применяя функциональный интерфейс.
        Collections.sort(usersFiltred, comparator);

        List<String> userNames = new LinkedList<>();
        for (User user: usersFiltred) {
            userNames.add(user.getName());
        }

        System.out.println(userNames);

        assertEquals("[Super woman, Super man, Super cat]", userNames.toString());

    }

    @Test
    public void streamTestDemoJava8() {

        Collection<User> users = Arrays.asList(
                new User(1,"Tema",Role.USER),
                new User(12,"Vasya Pupkin",Role.ADMIN),
                new User(133,"Super cat",Role.GUEST),
                new User(11,"Super woman", Role.GUEST),
                new User(94,"Super man",Role.GUEST)
        );

        List<String> userNames = users.stream()
                .filter(user -> user.getRole() == Role.GUEST)
                .sorted( (u1, u2) -> Long.compare(u1.getId(), u2.getId()))
                //Можно и так сортировать
                //.sorted(Comparator.comparing(User::getId).reversed())

                .map(user -> user.getName())
                //Можно так мапить
                //.map(User::getName)

                //терминальная функция
                .collect(Collectors.toList());


        assertEquals("[Super woman, Super man, Super cat]", userNames.toString());
    }

    @Test
    public void streamFilter() {

        Collection<User> users = Arrays.asList(
                new User(1,"Tema",Role.USER),
                new User(2,"Vasya Pupkin",Role.ADMIN),
                new User(3,"Super cat",Role.GUEST),
                new User(4,"Super woman", Role.GUEST),
                new User(5,"Super man",Role.GUEST)
        );

        List<String> userNames = users.stream()
                .filter(user -> user.getId() % 2 == 0)
                .map(User::getName)
                .collect(toList());

        assertEquals("[Vasya Pupkin, Super woman]", userNames.toString());
    }

    static class EqualsUser extends User {
        @Override

        public int hashCode() {
            return (int) (getId() ^ getId() >>> 32 );
        }

        public EqualsUser(long id, String name, Role role) {
            super(id, name, role);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;

            return getId() == user.getId();
        }
    }

    @Test
    public void distinctTest() {

        Collection<User> users = Arrays.asList(
                new EqualsUser(1,"User1",Role.USER),
                new EqualsUser(2,"User2",Role.ADMIN),
                new EqualsUser(2,"User3",Role.GUEST),
                new EqualsUser(1,"User4", Role.GUEST),
                new EqualsUser(5,"User5",Role.GUEST)
        );

        List<User> userList = users.stream()
                .unordered()
                .distinct()
                .collect(toList());

        System.out.println(userList);
    }

    @Test
    public void limitTest() {

        Collection<User> users = Arrays.asList(
                new EqualsUser(1,"User1",Role.USER),
                new EqualsUser(2,"User2",Role.ADMIN),
                new EqualsUser(2,"User3",Role.GUEST),
                new EqualsUser(1,"User4", Role.GUEST),
                new EqualsUser(5,"User5",Role.GUEST)
        );

        List<User> userList = users.stream()
                //отрезает последнии N элементов
                .limit(3)
                .collect(toList());

        System.out.println(userList);
    }

    @Test
    public void skipTest() {

        Collection<User> users = Arrays.asList(
                new EqualsUser(1,"User1",Role.USER),
                new EqualsUser(2,"User2",Role.ADMIN),
                new EqualsUser(3,"User3",Role.GUEST),
                new EqualsUser(4,"User4", Role.GUEST),
                new EqualsUser(5,"User5",Role.GUEST)
        );

        List<User> userList = users.stream()
                //отрезает первые N элементов
                .skip(3)
                .collect(toList());

        System.out.println(userList);
    }

    @Test
    public void sortedWithOutComparator() {

        Collection<User> users = Arrays.asList(
                new EqualsUser(4,"User4",Role.USER),
                new EqualsUser(1,"User1",Role.ADMIN),
                new EqualsUser(2,"User2",Role.GUEST),
                new EqualsUser(5,"User5", Role.GUEST),
                new EqualsUser(3,"User3",Role.GUEST)
        );

        List<User> userList = users.stream()
                //.sorted( (u1, u2) -> u1.getName().compareTo(u2.getName()))
                .sorted(Comparator.comparing(User::getName))
                .collect(toList());


        System.out.println(userList);
    }

    static class ComparableUser extends User implements Comparable<User> {

        public ComparableUser(long id, String name, Role role) {
            super(id, name, role);
        }


        @Override
        public int compareTo(User o) {
            return this.getName().compareTo(o.getName());
        }
    }

    @Test
    public void sortedWithComparator() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4,"User4",Role.USER),
                new ComparableUser(1,"User1",Role.ADMIN),
                new ComparableUser(2,"User2",Role.GUEST),
                new ComparableUser(5,"User5", Role.GUEST),
                new ComparableUser(3,"User3",Role.GUEST)
        );

        List<User> userList = users.stream()
                .sorted()
                .collect(toList());


        System.out.println(userList);
    }

    @Test
    public void streamOrder() {

        List<String> phases = new LinkedList<>();
        Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);


        List<Integer> finalColl = collection.stream()
                //.skip(5) обязательно вперед
                .filter(n-> {
                    phases.add("f-" + n);
                    return n % 2 ==0;
                })
                .map(m-> {
                    phases.add("m-" + m);
                    return m * m;
                })
                .sorted( (i1, i2) -> {
                    phases.add("s-" + i1 + "-" + i2 );
                    return Integer.compare(i1,i2);

                })
                .limit(2)
                .collect(toList());



        System.out.println(finalColl);
        System.out.println(phases);
    }

    @Test
    public void peekTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4,"User4",Role.USER),
                new ComparableUser(1,"User1",Role.ADMIN),
                new ComparableUser(2,"User2",Role.GUEST),
                new ComparableUser(5,"User5", Role.GUEST),
                new ComparableUser(3,"User3",Role.GUEST)
        );

        List<User> sorted = new LinkedList<>();
        List<String> names = users.stream()
                .filter(user -> user.getId() > 3)
                .sorted((u1,u2) -> u1.getName().compareTo(u2.getName()))
                .peek(user -> sorted.add(user))
                .map(user -> user.getName())
                .collect(toList());

        System.out.println(names);
        System.out.println(sorted);
    }

    @Test
    public void forEachTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4,"User4",Role.USER),
                new ComparableUser(1,"User1",Role.ADMIN),
                new ComparableUser(2,"User2",Role.GUEST),
                new ComparableUser(5,"User5", Role.GUEST),
                new ComparableUser(3,"User3",Role.GUEST)
        );

        List<Role> names = new LinkedList<>();

        users.stream()
                .map(user -> user.getRole())
                //.forEach(user -> names.add(user));
                .forEach(name -> System.out.println(name));
                //тоже самое через метод
                //.forEach(this::method);


        System.out.println(names);
    }

    public void method(Role role ) {
        System.out.println(role);
    }

    @Test
    public void mapTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4,"User4",Role.USER),
                new ComparableUser(1,"User1",Role.ADMIN),
                new ComparableUser(2,"User2",Role.GUEST),
                new ComparableUser(5,"User5", Role.GUEST),
                new ComparableUser(3,"User3",Role.GUEST)
        );

        List<Long> idList;
        List<String> names;

        names = users.stream()
                .map(user -> user.getName())
                .collect(toList());

        idList = users.stream()
                .map(user -> user.getId())
                .collect(toList());

        System.out.println(names);
        System.out.println(idList);
    }

    @Test
    public void testMapToInt_mapToObject() {

        List<User> users = Arrays.asList(
                new ComparableUser(4,"User4",Role.USER),
                new ComparableUser(1,"User1",Role.ADMIN),
                new ComparableUser(2,"User2",Role.GUEST),
                new ComparableUser(5,"User5", Role.GUEST),
                new ComparableUser(3,"User3",Role.GUEST)
        );

        List<String> lst = new ArrayList<>();

        List<User> userList = users.stream()
                .peek(user -> lst.add(user.getName()))
                //мапим в int
                .mapToInt(user -> (int ) user.getId())
                //мапим int в object
                .mapToObj(id -> new User(id, "User_" + id, users.get(id -1).getRole()))
                .collect(toList());

        System.out.println(userList);
        System.out.println(lst);

    }

    @Test
    public void testMapToDouble() {

        List<User> users = Arrays.asList(
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
        );

        double [] data = users.stream()
                .sorted( (u1, u2) -> u1.getRole().compareTo(u2.getRole()))
                .mapToDouble(user -> {
                    int roleLength = user.getRole().name().length();
                    return user.getId() + (double)roleLength;
                })
                .toArray();

        for(int i = 0; i < data.length; i++) {
            System.out.println(data[i]);
        }

        for(Double dbl: data) {
            System.out.println(dbl);
        }
    }

    @Test
    public void testFlatMap() {

        Collection<User> users = Arrays.asList(
                new User(4, "User4", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        List<String> data = users.stream()
                .sorted(Comparator.comparing(user -> user.getId()))
                .sorted((u1,u2) -> u1.getName().compareTo(u2.getName()))
                .flatMap(user -> Stream.of(
                        "id:" + user.getId(),
                        "role:" + user.getRole(),
                        "name:" + user.getName()))
                .collect(toList());

        System.out.println(data);


    }
    @Test
    public void testFlatMapToInt() {
        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        List<Integer> data = users.stream()
                //.sorted(Comparator.comparing(user -> user.getId()))
                .flatMapToInt(user -> IntStream.of(
                        (int) user.getId(),
                        user.getName().length(),
                        user.getRole().name().length()))
                //Нихуяшечки не понятно!
               .collect( () -> new LinkedList<Integer>(),
                        (list, value) -> list.add(value),
                       (list,list2 ) -> list.addAll(list2));

        System.out.println(data);


    }

}

