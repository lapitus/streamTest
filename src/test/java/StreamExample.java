import ch.qos.logback.core.boolex.EvaluationException;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;

import javax.jws.soap.SOAPBinding;
import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
                new User(1, "Tema", Role.USER),
                new User(12, "Vasya Pupkin", Role.ADMIN),
                new User(133, "Super cat", Role.GUEST),
                new User(11, "Super woman", Role.GUEST),
                new User(94, "Super man", Role.GUEST)
        );

        //Так фильтруем данные.
        List<User> usersFiltred = new LinkedList<>();

        for (User user : users) {
            if (user.getRole() == Role.GUEST) {
                usersFiltred.add(user);
            }
        }

        //Так сортируем данные.
        Comparator<User> comparator = (u1, u2) -> Long.compare(u1.getId(), u2.getId());

        Collections.sort(usersFiltred, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Long.compare(o1.getId(), o2.getId());
            }
        });

        //Сортировать можно так, применяя функциональный интерфейс.
        Collections.sort(usersFiltred, comparator);

        List<String> userNames = new LinkedList<>();
        for (User user : usersFiltred) {
            userNames.add(user.getName());
        }

        System.out.println(userNames);

        assertEquals("[Super woman, Super man, Super cat]", userNames.toString());

    }

    @Test
    public void streamTestDemoJava8() {

        Collection<User> users = Arrays.asList(
                new User(1, "Tema", Role.USER),
                new User(12, "Vasya Pupkin", Role.ADMIN),
                new User(133, "Super cat", Role.GUEST),
                new User(11, "Super woman", Role.GUEST),
                new User(94, "Super man", Role.GUEST)
        );

        List<String> userNames = users.stream()
                .filter(user -> user.getRole() == Role.GUEST)
                .sorted((u1, u2) -> Long.compare(u1.getId(), u2.getId()))
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
                new User(1, "Tema", Role.USER),
                new User(2, "Vasya Pupkin", Role.ADMIN),
                new User(3, "Super cat", Role.GUEST),
                new User(4, "Super woman", Role.GUEST),
                new User(5, "Super man", Role.GUEST)
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
            return (int) (getId() ^ getId() >>> 32);
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
                new EqualsUser(1, "User1", Role.USER),
                new EqualsUser(2, "User2", Role.ADMIN),
                new EqualsUser(2, "User3", Role.GUEST),
                new EqualsUser(1, "User4", Role.GUEST),
                new EqualsUser(5, "User5", Role.GUEST)
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
                new EqualsUser(1, "User1", Role.USER),
                new EqualsUser(2, "User2", Role.ADMIN),
                new EqualsUser(2, "User3", Role.GUEST),
                new EqualsUser(1, "User4", Role.GUEST),
                new EqualsUser(5, "User5", Role.GUEST)
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
                new EqualsUser(1, "User1", Role.USER),
                new EqualsUser(2, "User2", Role.ADMIN),
                new EqualsUser(3, "User3", Role.GUEST),
                new EqualsUser(4, "User4", Role.GUEST),
                new EqualsUser(5, "User5", Role.GUEST)
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
                new EqualsUser(4, "User4", Role.USER),
                new EqualsUser(1, "User1", Role.ADMIN),
                new EqualsUser(2, "User2", Role.GUEST),
                new EqualsUser(5, "User5", Role.GUEST),
                new EqualsUser(3, "User3", Role.GUEST)
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
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
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
                .filter(n -> {
                    phases.add("f-" + n);
                    return n % 2 == 0;
                })
                .map(m -> {
                    phases.add("m-" + m);
                    return m * m;
                })
                .sorted((i1, i2) -> {
                    phases.add("s-" + i1 + "-" + i2);
                    return Integer.compare(i1, i2);

                })
                .limit(2)
                .collect(toList());


        System.out.println(finalColl);
        System.out.println(phases);
    }

    @Test
    public void peekTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
        );

        List<User> sorted = new LinkedList<>();
        List<String> names = users.stream()
                .filter(user -> user.getId() > 3)
                .sorted((u1, u2) -> u1.getName().compareTo(u2.getName()))
                .peek(user -> sorted.add(user))
                .map(user -> user.getName())
                .collect(toList());

        System.out.println(names);
        System.out.println(sorted);
    }

    @Test
    public void forEachTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
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

    public void method(Role role) {
        System.out.println(role);
    }

    @Test
    public void mapTest() {

        Collection<User> users = Arrays.asList(
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
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
                new ComparableUser(4, "User4", Role.USER),
                new ComparableUser(1, "User1", Role.ADMIN),
                new ComparableUser(2, "User2", Role.GUEST),
                new ComparableUser(5, "User5", Role.GUEST),
                new ComparableUser(3, "User3", Role.GUEST)
        );

        List<String> lst = new ArrayList<>();

        List<User> userList = users.stream()
                .peek(user -> lst.add(user.getName()))
                //мапим в int
                .mapToInt(user -> (int) user.getId())
                //мапим int в object
                .mapToObj(id -> new User(id, "User_" + id, users.get(id - 1).getRole()))
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

        double[] data = users.stream()
                .sorted((u1, u2) -> u1.getRole().compareTo(u2.getRole()))
                .mapToDouble(user -> {
                    int roleLength = user.getRole().name().length();
                    return user.getId() + (double) roleLength;
                })
                .toArray();

        for (int i = 0; i < data.length; i++) {
            System.out.println(data[i]);
        }

        for (Double dbl : data) {
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
                .sorted((u1, u2) -> u1.getName().compareTo(u2.getName()))
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
                .collect(() -> new LinkedList<Integer>(),
                        (list, value) -> list.add(value),
                        (list, list2) -> list.addAll(list2));

        System.out.println(data);

    }
    //flatMapToLong
    //flatMapToDouble
    //аналогично int

    @Test
    public void testFlatMapToDouble() {
        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        List<Double> data = users.stream()
                //.sorted(Comparator.comparing(user -> user.getId()))
                .flatMapToDouble(user -> DoubleStream.of(
                        (double) user.getId(),
                        user.getRole().name().length(),
                        user.getName().length()))
                //Нихуяшечки не понятно!
                .collect(() -> new LinkedList<Double>(),
                        (list, value) -> list.add(value),
                        (list, list2) -> list.addAll(list2));

        System.out.println(data);

    }

    @Test
    public void testFlatMapToLong() {
        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        List<Long> data = users.stream()
                //.sorted(Comparator.comparing(user -> user.getId()))
                .flatMapToLong(user -> LongStream.of(
                        user.getId(),
                        user.getRole().name().length(),
                        user.getName().length()))
                //Нихуяшечки не понятно!
                .collect(() -> new LinkedList<Long>(),
                        (list, value) -> list.add(value),
                        (list, list2) -> list.addAll(list2));

        System.out.println(data);

    }

    @Test
    public void toArrayTest() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Object[] sorted = users.stream()
                .sorted(Comparator.comparing(user -> user.getName()))
                .toArray();

        for (Object obj : sorted) {
            System.out.println(obj);
        }

    }

    @Test
    public void testCollect() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

//        List<String> data = users.stream()
//                .sorted(Comparator.comparing(user -> user.getName()))
//                .map(user -> user.toString())
//                .collect(
//                        //Это Supplier создает контейнер
//                        () -> new LinkedList<>(),
//                        //Это biconsumer (Accumulator) добавляет один элемент в контейнер.
//                        (list, value) -> list.add(value),
//                        //Это BiConsumer (Combiner) добавляет все элементы в контейнер.
//                        (list, list2) -> list.addAll(list2));

        //same with method reference
//        List<String> data = users.stream()
//                .sorted(Comparator.comparing(User::getName).reversed())
//                .map(User::toString)
//                .collect(
//                        //Это Supplier создает контейнер
//                        LinkedList<String>::new,
//                        //Это biconsumer (Accumulator) добавляет один элемент в контейнер.
//                        List::add,
//                        //Это BiConsumer (Combiner) добавляет все элементы в контейнер.
//                        List::addAll
//        );

        Set<String> data = users.stream()
                .sorted(Comparator.comparing(User::getName).reversed())
                .map(User::toString)
                .collect(
                        //Это Supplier создает контейнер
                        () -> new HashSet<String>(),
                        //Это biconsumer (Accumulator) добавляет один элемент в контейнер.
                        (list, value) -> list.add(value),
                        //Это BiConsumer (Combiner) добавляет все элементы в контейнер.
                        (list, list2) -> list.addAll(list2)
                );

        System.out.println(data);

        for (String str : data) {
            System.out.println(str);
        }

        System.out.println(data.size());
    }

    @Test
    public void test() {
        List<User> users = Arrays.asList(
                new User(1,"Vasya",Role.ADMIN)
        );

        for (User user: users) {
            System.out.println(user);
        }

        String admin = Role.ADMIN.toString();
        System.out.println(admin);
    }

    @Test
    public void testCollectorToList() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        List<String> userNames = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getName))
                .map(user -> user.getName())
                .collect(toList());
        System.out.println(userNames);
    }

    @Test
    public void testCollectorToSet() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Set<String> userNames = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getId))
                .map(user -> user.getName())
                .collect(toSet());
        System.out.println(userNames);

        for(String user: userNames) {
            System.out.println(user);
        }
    }

    @Test
    public void testCollectorToMap() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Map<Long, String> userNames = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getName()
                ));

        System.out.println(userNames);

        userNames.forEach( (k,v) -> System.out.println("Key = " + k + " Value = " + v));

    }

    @Test
    public void testCollectorColection() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Collection<User> userNames = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getId))
        .collect(Collectors.toCollection(HashSet::new
        //.collect(Collectors.toCollection( () -> new HashSet<User>()
                ));

        System.out.println(userNames);
    }

    @Test
    public void testCollectorJoining() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        String userNames = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getId))
                .map(user -> user.getName())
                .collect(Collectors.joining());

        System.out.println(userNames);
    }

    @Test
    public void testCollectorCounting() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Long userCnt = users.stream()
                //.filter(user -> user.getName().equals("User1"))
                .sorted(Comparator.comparing(User::getId))
                .map(user -> user.getName())
                .collect(Collectors.counting());

        System.out.println(userCnt);
    }

    @Test
    public void testCollectorMinMax() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Optional userCnt = users.stream()
                  .map(user-> user.getName())
                  .collect(Collectors.maxBy(Comparator.naturalOrder()));
                 //.collect(Collectors.minBy(Comparator.naturalOrder()));

        System.out.println(userCnt.get());
    }

    @Test
    public void testCollectorSummingInt() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        int sumId = users.stream()
                .collect(Collectors.summingInt(user -> (int) user.getId()));
        System.out.println(sumId);
    }

    @Test
    public void testCollectorSummingDouble() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        double sumId = users.stream()
                .collect(Collectors.summingDouble(user -> (double) user.getId()));
        System.out.println(sumId);
    }

    @Test
    public void testCollectorSummingLong() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        double sumId = users.stream()
                .collect(Collectors.summarizingLong(User::getId)).getSum();
        System.out.println(sumId);
    }

    @Test
    public void testCollectorAvg() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        double avg = users.stream()
                .collect(Collectors.averagingInt(user -> (int) user.getId()));
        System.out.println(avg);
    }

    @Test
    public void testCollectorAvgLong() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        double avg = users.stream()
                .collect(Collectors.averagingLong(user -> user.getId()));
        System.out.println(avg);
    }

    @Test
    public void testReduce() {

        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        double userReduce = users.stream()
                .map(user -> user.getId())
                .collect(Collectors.reducing((u1, u2) -> u1 - u2)).orElse(-1L);

        System.out.println(userReduce);

    }

    static String removeDuplicates(String s) {
        StringBuilder noDupes = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            String si = s.substring(i, i + 1);
            if (noDupes.indexOf(si) == -1) {
                noDupes.append(si);
            }
        }
        return noDupes.toString();
    }

    @Test
    public void testReduceWithLogin() {



        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        String userNamesWithoutRepeateSymbol = users.stream()
                .map(User::getName)
                .reduce( "", (u1,u2) -> removeDuplicates(u1 + u2));

        System.out.println(userNamesWithoutRepeateSymbol);

    }

    class ClassUserWithCompany extends User {

        private String company;

        public String getCompany() {
            return company;
        }

        public ClassUserWithCompany(long id, String name, Role role) {
            super(id, name, role);

        }
        public ClassUserWithCompany(long id, String name, Role role, String company) {
            super(id, name, role);
            this.company = company;
        }
    }

    @Test
    public void testGroupingBy() {


        Collection<ClassUserWithCompany> users = Arrays.asList(
                new ClassUserWithCompany(4, "User44", Role.USER, "Microsoft"),
                new ClassUserWithCompany(4, "User44", Role.ADMIN, "Google"),
                new ClassUserWithCompany(1, "User1", Role.ADMIN, "Google"),
                new ClassUserWithCompany(2, "User2", Role.GUEST, "123"),
                new ClassUserWithCompany(5, "User5", Role.GUEST, "Google"),
                new ClassUserWithCompany(5, "User5", Role.ADMIN, "Microsoft"),
                new ClassUserWithCompany(3, "User3", Role.GUEST,"123")
        );

        Map<String, List<ClassUserWithCompany>> userColl = users.stream()
                .collect(Collectors.groupingBy((u1) -> u1.getCompany())
                );

        userColl.forEach((k,v ) ->  System.out.println("K = " + k + " V = " + v));
    }

    @Test
    public void testReduceWithStartingPoing() {



        Collection<User> users = Arrays.asList(
                new User(4, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Long sum = users.stream()
                .map(User::getId)
                .reduce( 350L, (u1,u2) -> u1 + u2);

        System.out.println(sum);

    }

    @Test
    public void testMinMax() {



        Collection<User> users = Arrays.asList(
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Optional<User> minUser = users.stream()
                .max(Comparator.comparing(User::getId)
                        .thenComparing(user -> user.getName()));
        System.out.println(minUser.get());
    }

    @Test
    public void testMinCount() {



        Collection<User> users = Arrays.asList(
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Long result = users.stream()
                .filter(user -> user.getRole().name().equals("GUEST"))
                .count();

        System.out.println(result);
    }

    @Test
    public void testAnyMatch() {



        Collection<User> users = Arrays.asList(
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        assertEquals(true,users.stream().anyMatch(user -> user.getRole().equals(Role.ADMIN)));

        assertEquals(true,users.stream().anyMatch(user -> user.getId() == 5));
    }

    @Test
    public void testFindFirst() {



        Collection<User> users = Arrays.asList(
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Optional<User> findUser = users.stream()
                .findFirst();

        System.out.println(findUser.orElseGet(() -> new User(1,"aaa",Role.ADMIN)));

    }
    @Test
    public void testStreamOF() {
        Stream<User> stream = Stream.of(
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        );

        Optional<User> user = stream.findAny();
        System.out.println(user.get().getName());
    }

    @Test
    public void testStreamBuilder() {
        Stream<User> stream = Stream.<User>builder()
                .add(new User(0, "User44", Role.USER))
                .add(new User(1, "User1", Role.ADMIN))
                .add(new User(2, "User2", Role.GUEST))
                .add(new User(5, "User5", Role.GUEST))
                .add(new User(3, "User3", Role.GUEST)).build();

        Optional<User> user = stream.findAny();
        System.out.println(user.get().getName());
    }

    @Test
    public void testArrayToStream() {
        User[] usersArray = {
                new User(0, "User44", Role.USER),
                new User(1, "User1", Role.ADMIN),
                new User(2, "User2", Role.GUEST),
                new User(5, "User5", Role.GUEST),
                new User(3, "User3", Role.GUEST)
        };

        Stream<User> usersStream = Arrays.stream(usersArray);
    }

    @Test
    public void testStreamItterate() {

        int start = 0;


        List<Integer> result = Stream
                .iterate(start, n -> n + 1)
                .limit(50)
                .collect(toList());

        System.out.println(result);
    }

    @Test
    public void testStreamGenerate() {

        Random random = new Random();

        List<User> result = Stream
                .generate(() -> new User(random.nextInt(99),"vasya_" + random.nextInt(54),Role.values()[random.nextInt(3)]))
                .limit(50)
                .collect(toList());

        System.out.println(result);
    }

    @Test
    public void testStreamConcat() {

        Stream<Integer> stream1 = Stream
                .iterate(0, n -> n +1)
                .limit(25);

        Stream<Integer> stream2 = Stream
                .iterate(100, n -> n +1)
                .limit(25);

//        Stream<Integer> stream3 = Stream.concat(stream1,stream2);
//
//        for (Integer integer: stream3.collect(toList())){
//            System.out.print(integer + " ");
//        }

        List<Integer> list = Stream.concat(stream1,stream2).collect(toList());
        System.out.println(list);
    }



}

