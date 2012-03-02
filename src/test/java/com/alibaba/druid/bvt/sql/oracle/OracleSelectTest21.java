package com.alibaba.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alibaba.druid.sql.OracleTest;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

public class OracleSelectTest21 extends OracleTest {

    public void test_0() throws Exception {
        String sql = //
        "SELECT EVENT, WAITS, TIME, DECODE(WAITS, NULL, TO_NUMBER(NULL), 0, TO_NUMBER(NULL), TIME/WAITS*1000) AVGWT" + //
                "   , PCTWTT, WAIT_CLASS " + "FROM (SELECT EVENT, WAITS, TIME, PCTWTT, WAIT_CLASS " + //
                "       FROM (" + //
                "           SELECT E.EVENT_NAME EVENT, E.TOTAL_WAITS - NVL(B.TOTAL_WAITS,0) WAITS" + //
                "              , (E.TIME_WAITED_MICRO - NVL(B.TIME_WAITED_MICRO,0)) / 1000000 TIME" + //
                "              , 100 * (E.TIME_WAITED_MICRO - NVL(B.TIME_WAITED_MICRO,0)) / :B1 PCTWTT" + //
                "           , E.WAIT_CLASS WAIT_CLASS " + //
                "           FROM DBA_HIST_SYSTEM_EVENT B, DBA_HIST_SYSTEM_EVENT E " + //
                "           WHERE B.SNAP_ID(+) = :B5 AND E.SNAP_ID = :B4 AND B.DBID(+) = :B3 AND E.DBID = :B3 " + //
                "           AND B.INSTANCE_NUMBER(+) = :B2 AND E.INSTANCE_NUMBER = :B2 AND B.EVENT_ID(+) = E.EVENT_ID " + //
                "           AND E.TOTAL_WAITS > NVL(B.TOTAL_WAITS,0) AND E.WAIT_CLASS != 'Idle' " + //
                "          UNION ALL " + //
                "           SELECT 'CPU time' EVENT, TO_NUMBER(NULL) WAITS" + //
                "                   , :B6 /1000000 TIME, 100 * :B6 / :B1 PCTWTT, NULL WAIT_CLASS FROM DUAL WHERE :B6 > 0" + //
                ") ORDER BY TIME DESC, WAITS DESC) " + //
                "WHERE ROWNUM <= :B7 "; //

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        print(statementList);

        Assert.assertEquals(1, statementList.size());

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("relationships : " + visitor.getRelationships());
        System.out.println("orderBy : " + visitor.getOrderByColumns());

        Assert.assertEquals(1, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("\"DUAL\"")));

        Assert.assertEquals(0, visitor.getColumns().size());

        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "*")));
        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "YEAR")));
        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "order_mode")));
    }
}
