package io.github.iamazy.elasticsearch.dsl.sql.parser.query.exact;

import io.github.iamazy.elasticsearch.dsl.antlr4.ElasticsearchParser;
import io.github.iamazy.elasticsearch.dsl.sql.enums.SqlConditionOperator;
import io.github.iamazy.elasticsearch.dsl.sql.model.AtomicQuery;
import io.github.iamazy.elasticsearch.dsl.utils.StringManager;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * @author iamazy
 * @date 2019/7/29
 * @descrition
 **/
public class BetweenAndQueryParser extends AbstractExactQueryParser {


    public AtomicQuery parse(ElasticsearchParser.BetweenAndContext betweenAndContext) {
        return parseCondition(betweenAndContext, SqlConditionOperator.BetweenAnd,
                new Object[]{betweenAndContext.left.getText(), betweenAndContext.right.getText()},
                (field, operator, params) -> QueryBuilders.rangeQuery(field).gte(StringManager.removeStringSymbol(params[0].toString())).lte(StringManager.removeStringSymbol(params[1].toString())));
    }
}
















