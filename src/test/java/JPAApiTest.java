import club.haominglfs.entity.many2many.Category;
import club.haominglfs.entity.many2many.Item;
import club.haominglfs.entity.one2many.Customer;
import club.haominglfs.entity.one2many.Order;
import org.hibernate.jpa.QueryHints;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @Author: haoming
 * @Date: 2019/2/27 9:01 PM
 * @Version 1.0
 */
public class JPAApiTest {

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private EntityTransaction transaction;

    @Before
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("jpa");
        entityManager = entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
    }

    @After
    public void destroy() {
        transaction.commit();
        entityManager.close();
        entityManagerFactory.close();
    }

    //可以使用 JPQL 完成 UPDATE 和 DELETE 操作.
    @Test
    public void testExecuteUpdate() {
        String jpql = "UPDATE Customer c SET c.lastName = ? WHERE c.id = ?";
        Query query = entityManager.createQuery(jpql).setParameter(1, "YYY").setParameter(2, 12);

        query.executeUpdate();
    }

    //使用 jpql 内建的函数
    @Test
    public void testJpqlFunction() {
        String jpql = "SELECT lower(c.email) FROM Customer c";

        List<String> emails = entityManager.createQuery(jpql).getResultList();
        System.out.println(emails);
    }

    @Test
    public void testSubQuery() {
        //查询所有 Customer 的 lastName 为 YY 的 Order
        String jpql = "SELECT o FROM Order o "
                + "WHERE o.customer = (SELECT c FROM Customer c WHERE c.lastName = ?)";

        Query query = entityManager.createQuery(jpql).setParameter(1, "YY");
        List<Order> orders = query.getResultList();
        System.out.println(orders.size());
    }

    /**
     * JPQL 的关联查询同 HQL 的关联查询.
     */
    @Test
    public void testLeftOuterJoinFetch() {
        String jpql = "FROM Customer c LEFT OUTER JOIN FETCH c.orders WHERE c.id = ?";

        Customer customer =
                (Customer) entityManager.createQuery(jpql).setParameter(1, 12).getSingleResult();
        System.out.println(customer.getLastName());
        System.out.println(customer.getOrders().size());

//		List<Object[]> result = entityManager.createQuery(jpql).setParameter(1, 12).getResultList();
//		System.out.println(result);
    }

    //查询 order 数量大于 2 的那些 Customer
    @Test
    public void testGroupBy() {
        String jpql = "SELECT o.customer FROM Order o "
                + "GROUP BY o.customer "
                + "HAVING count(o.id) >= 2";
        List<Customer> customers = entityManager.createQuery(jpql).getResultList();

        System.out.println(customers);
    }

    @Test
    public void testOrderBy() {
        String jpql = "FROM Customer c WHERE c.age > ? ORDER BY c.age DESC";
        Query query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);

        //占位符的索引是从 1 开始
        query.setParameter(1, 1);
        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());
    }

    //使用 hibernate 的查询缓存.
    @Test
    public void testQueryCache() {
        String jpql = "FROM Customer c WHERE c.age > ?";
        Query query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);

        //占位符的索引是从 1 开始
        query.setParameter(1, 1);
        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());

        query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);

        //占位符的索引是从 1 开始
        query.setParameter(1, 1);
        customers = query.getResultList();
        System.out.println(customers.size());
    }

    //createNativeQuery 适用于本地 SQL
    @Test
    public void testNativeQuery() {
        String sql = "SELECT age FROM jpa_cutomers WHERE id = ?";
        Query query = entityManager.createNativeQuery(sql).setParameter(1, 3);

        Object result = query.getSingleResult();
        System.out.println(result);
    }

    //createNamedQuery 适用于在实体类前使用 @NamedQuery 标记的查询语句
    @Test
    public void testNamedQuery() {
        Query query = entityManager.createNamedQuery("testNamedQuery").setParameter(1, 3);
        Customer customer = (Customer) query.getSingleResult();

        System.out.println(customer);
    }

    //默认情况下, 若只查询部分属性, 则将返回 Object[] 类型的结果. 或者 Object[] 类型的 List.
    //也可以在实体类中创建对应的构造器, 然后再 JPQL 语句中利用对应的构造器返回实体类的对象.
    @Test
    public void testPartlyProperties() {
        String jpql = "SELECT new Customer(c.lastName, c.age) FROM Customer c WHERE c.id > ?";
        List result = entityManager.createQuery(jpql).setParameter(1, 1).getResultList();

        System.out.println(result);
    }

    @Test
    public void testHelloJPQL() {
        String jpql = "FROM Customer c WHERE c.age > ?";
        Query query = entityManager.createQuery(jpql);

        //占位符的索引是从 1 开始
        query.setParameter(1, 1);
        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());
    }

    @Test
    public void testSecondLevelCache() {
        Customer customer1 = entityManager.find(Customer.class, 1);

        transaction.commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        Customer customer2 = entityManager.find(Customer.class, 1);
    }















    /**
     * 同 hibernate 中 Session 的 refresh 方法.
     */
    @Test
    public void testRefresh() {
        Customer customer = entityManager.find(Customer.class, 1);
        customer = entityManager.find(Customer.class, 1);

        entityManager.refresh(customer);
    }

    /**
     * 同 hibernate 中 Session 的 flush 方法.
     */
    @Test
    public void testFlush() {
        Customer customer = entityManager.find(Customer.class, 1);
        System.out.println(customer);

        customer.setLastName("AA");

        entityManager.flush();
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中有对应的对象
    //2. JPA 会把游离对象的属性复制到查询到EntityManager 缓存中的对象中.
    //3. EntityManager 缓存中的对象执行 UPDATE.
    @Test
    public void testMerge4() {
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("dd@163.com");
        customer.setLastName("DD");

        customer.setId(4);
        Customer customer2 = entityManager.find(Customer.class, 4);

        entityManager.merge(customer);

        System.out.println(customer == customer2); //false
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中没有该对象
    //2. 若在数据库中也有对应的记录
    //3. JPA 会查询对应的记录, 然后返回该记录对一个的对象, 再然后会把游离对象的属性复制到查询到的对象中.
    //4. 对查询到的对象执行 update 操作.
    @Test
    public void testMerge3() {
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("ee@163.com");
        customer.setLastName("EE");

        customer.setId(4);

        Customer customer2 = entityManager.merge(customer);

        System.out.println(customer == customer2); //false
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中没有该对象
    //2. 若在数据库中也没有对应的记录
    //3. JPA 会创建一个新的对象, 然后把当前游离对象的属性复制到新创建的对象中
    //4. 对新创建的对象执行 insert 操作.
    @Test
    public void testMerge2() {
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("dd@163.com");
        customer.setLastName("DD");

        customer.setId(100);

        Customer customer2 = entityManager.merge(customer);

        System.out.println("customer#id:" + customer.getId());
        System.out.println("customer2#id:" + customer2.getId());
    }

    /**
     * 总的来说: 类似于 hibernate Session 的 saveOrUpdate 方法.
     */
    //1. 若传入的是一个临时对象
    //会创建一个新的对象, 把临时对象的属性复制到新的对象中, 然后对新的对象执行持久化操作. 所以
    //新的对象中有 id, 但以前的临时对象中没有 id.
    @Test
    public void testMerge1() {
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("cc@163.com");
        customer.setLastName("CC");

        Customer customer2 = entityManager.merge(customer);

        System.out.println("customer#id:" + customer.getId());
        System.out.println("customer2#id:" + customer2.getId());
    }

    //类似于 hibernate 中 Session 的 delete 方法. 把对象对应的记录从数据库中移除
    //但注意: 该方法只能移除 持久化 对象. 而 hibernate 的 delete 方法实际上还可以移除 游离对象.
    @Test
    public void testRemove() {
//		Customer customer = new Customer();
//		customer.setId(2);

        Customer customer = entityManager.find(Customer.class, 2);
        entityManager.remove(customer);
    }

    //类似于 hibernate 的 save 方法. 使对象由临时状态变为持久化状态.
    //和 hibernate 的 save 方法的不同之处: 若对象有 id, 则不能执行 insert 操作, 而会抛出异常.
    @Test
    public void testPersistence() {
        Customer customer = new Customer();
        customer.setAge(15);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("bb@163.com");
        customer.setLastName("BB");
        customer.setId(100);

        entityManager.persist(customer);
        System.out.println(customer.getId());
    }

    //类似于 hibernate 中 Session 的 load 方法
    @Test
    public void testGetReference() {
        Customer customer = entityManager.getReference(Customer.class, 3);
        System.out.println(customer.getClass().getName());

        System.out.println("-------------------------------------");
//		transaction.commit();
//		entityManager.close();

        System.out.println(customer);
    }

    //类似于 hibernate 中 Session 的 get 方法.
    @Test
    public void testFind() {
        Customer customer = entityManager.find(Customer.class, 1);
        System.out.println("-------------------------------------");

        System.out.println(customer);
    }

}

