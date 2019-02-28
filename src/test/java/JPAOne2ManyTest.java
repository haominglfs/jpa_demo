import club.haominglfs.entity.one2many.Customer;
import club.haominglfs.entity.one2many.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Date;

/**
 * @Author: haoming
 * @Date: 2019/2/28 9:03 PM
 * @Version 1.0
 */
public class JPAOne2ManyTest {
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

    //若是双向 1-n 的关联关系, 执行保存时
    //若先保存 n 的一端, 再保存 1 的一端, 默认情况下, 会多出 n 条 UPDATE 语句.
    //若先保存 1 的一端, 则会多出 n 条 UPDATE 语句
    //在进行双向 1-n 关联关系时, 建议使用 n 的一方来维护关联关系, 而 1 的一方不维护关联系, 这样会有效的减少 SQL 语句.
    //注意: 若在 1 的一端的 @OneToMany 中使用 mappedBy 属性, 则 @OneToMany 端就不能再使用 @JoinColumn 属性了.

    //单向 1-n 关联关系执行保存时, 一定会多出 UPDATE 语句.
    //因为 n 的一端在插入时不会同时插入外键列.
    @Test
    public void testOneToManyPersist() {
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("mm@163.com");
        customer.setLastName("MM");

        Order order1 = new Order();
        order1.setOrderName("O-MM-1");

        Order order2 = new Order();
        order2.setOrderName("O-MM-2");

        //建立关联关系
        customer.getOrders().add(order1);
        customer.getOrders().add(order2);

        order1.setCustomer(customer);
        order2.setCustomer(customer);

        //执行保存操作
        entityManager.persist(customer);

        entityManager.persist(order1);
        entityManager.persist(order2);
    }

    //默认情况下, 若删除 1 的一端, 则会先把关联的 n 的一端的外键置空, 然后进行删除.
    //可以通过 @OneToMany 的 cascade 属性来修改默认的删除策略.
    @Test
    public void testOneToManyRemove() {
        Customer customer = entityManager.find(Customer.class, 8);
        entityManager.remove(customer);
    }

    //默认对关联的多的一方使用懒加载的加载策略.
    //可以使用 @OneToMany 的 fetch 属性来修改默认的加载策略
    @Test
    public void testOneToManyFind() {
        Customer customer = entityManager.find(Customer.class, 3);
        System.out.println(customer.getLastName());

        System.out.println(customer.getOrders().size());
    }

    @Test
    public void testManyToOneUpdate(){
        Order order = entityManager.find(Order.class, 4);
        order.getCustomer().setLastName("FFF");
    }

    @Test
    public void testUpdate() {
        Customer customer = entityManager.find(Customer.class, 10);
        customer.getOrders().iterator().next().setOrderName("O-XXX-10");
    }

    //不能直接删除 1 的一端, 因为有外键约束.
    @Test
    public void testManyToOneRemove(){
//		Order order = entityManager.find(Order.class, 1);
//		entityManager.remove(order);

        Customer customer = entityManager.find(Customer.class, 7);
        entityManager.remove(customer);
    }

    //默认情况下, 使用左外连接的方式来获取 n 的一端的对象和其关联的 1 的一端的对象.
    //可使用 @ManyToOne 的 fetch 属性来修改默认的关联属性的加载策略
    @Test
    public void testManyToOneFind(){
        Order order = entityManager.find(Order.class, 1);
        System.out.println(order.getOrderName());

        System.out.println(order.getCustomer().getLastName());
    }

    /**
     * 保存多对一时, 建议先保存 1 的一端, 后保存 n 的一端, 这样不会多出额外的 UPDATE 语句.
     */

	@Test
	public void testManyToOnePersist(){
		Customer customer = new Customer();
		customer.setAge(18);
		customer.setBirth(new Date());
		customer.setCreatedTime(new Date());
		customer.setEmail("gg@163.com");
		customer.setLastName("GG");

		Order order1 = new Order();
		order1.setOrderName("G-GG-1");

		Order order2 = new Order();
		order2.setOrderName("G-GG-2");

		//设置关联关系
		order1.setCustomer(customer);
		order2.setCustomer(customer);

		//执行保存操作
		entityManager.persist(order1);
		entityManager.persist(order2);

		entityManager.persist(customer);
	}
}
