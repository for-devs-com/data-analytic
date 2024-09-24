package com.fordevs.databridge.processor;//package com.fordevs.databridge.processor;
//
//import com.fordevs.databridge.entity.mysql.MySqlStudent;
//import com.fordevs.databridge.entity.postgresql.PostgreSqlStudent;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PostgresqlToMysqlProcessor implements ItemProcessor<PostgreSqlStudent, MySqlStudent> {
//
//    @Override
//    public MySqlStudent process(PostgreSqlStudent postgreSqlStudent) {
//        System.out.println(postgreSqlStudent.getId());
//        MySqlStudent mySqlStudent = new MySqlStudent();
//        mySqlStudent.setId(postgreSqlStudent.getId());
//        mySqlStudent.setFirstName(postgreSqlStudent.getFirstName());
//        mySqlStudent.setLastName(postgreSqlStudent.getLastName());
//        mySqlStudent.setEmail(postgreSqlStudent.getEmail());
//        mySqlStudent.setDeptId(postgreSqlStudent.getDeptId());
//        mySqlStudent.setIsActive(postgreSqlStudent.getIsActive() != null ? Boolean.valueOf(postgreSqlStudent.getIsActive()) : false);
//        return mySqlStudent;
//    }
//
//}
