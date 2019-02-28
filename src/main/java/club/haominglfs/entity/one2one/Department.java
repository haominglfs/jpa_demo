package club.haominglfs.entity.one2one;

import javax.persistence.*;

/**
 * @Author: haoming
 * @Date: 2019/2/27 9:08 PM
 * @Version 1.0
 */
@Table(name="JPA_DEPARTMENTS")
@Entity
public class Department {

    private Integer id;
    private String deptName;

    private Manager mgr;

    /*
        @GeneratedValue  用于标注主键的生成策略,通过strategy属性指定。
        默认情况下,JPA自动选择一个最适合底层数据库的主键生成策略：SqlServer对应identity，MySQL对应auto increment
        在 javax.persistence.GenerationType 中定义了以下几种可供选择的策略：
            IDENTITY：采用数据库ID自增长的方式来自增主键字段，Oracle 不支持这种方式；
            AUTO： JPA自动选择合适的策略，是默认选项；
            SEQUENCE：通过序列产生主键，通过 @SequenceGenerator注解指定序列名，MySql不支持这种方式
            TABLE：通过表产生主键，框架借由表模拟序列产生主键，使用该策略可以使应用更易于数据库移植
     */

    @GeneratedValue
    @Id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name="DEPT_NAME")
    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    //使用 @OneToOne 来映射 1-1 关联关系。
    //若需要在当前数据表中添加主键则需要使用 @JoinColumn 来进行映射. 注意, 1-1 关联关系, 所以需要添加 unique=true
    @JoinColumn(name="MGR_ID", unique=true)
    @OneToOne(fetch=FetchType.LAZY)
    public Manager getMgr() {
        return mgr;
    }

    public void setMgr(Manager mgr) {
        this.mgr = mgr;
    }
}

