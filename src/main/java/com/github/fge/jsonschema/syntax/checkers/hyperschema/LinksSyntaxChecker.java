package com.github.fge.jsonschema.syntax.checkers.hyperschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.syntax.checkers.AbstractSyntaxChecker;
import com.github.fge.jsonschema.syntax.checkers.SyntaxChecker;
import com.github.fge.jsonschema.tree.SchemaTree;
import com.github.fge.uritemplate.URITemplate;
import com.github.fge.uritemplate.URITemplateParseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.github.fge.jsonschema.messages.SyntaxMessages.*;

public final class LinksSyntaxChecker
    extends AbstractSyntaxChecker
{
    private static final List<String> REQUIRED_LDO_PROPERTIES
        = ImmutableList.of("href", "rel");

    private static final SyntaxChecker INSTANCE = new LinksSyntaxChecker();

    private LinksSyntaxChecker()
    {
        super("links", NodeType.ARRAY);
    }

    public static SyntaxChecker getInstance()
    {
        return INSTANCE;
    }

    @Override
    protected void checkValue(final Collection<JsonPointer> pointers,
        final ProcessingReport report, final SchemaTree tree)
        throws ProcessingException
    {
        final JsonNode node = getNode(tree);
        final int size = node.size();

        JsonNode ldo;
        NodeType type;
        ProcessingMessage msg;
        Set<String> set;
        List<String> list;

        for (int index = 0; index < size; index++) {
            ldo = getNode(tree).get(index);
            msg = newMsg(tree, index);
            type = NodeType.getNodeType(ldo);
            if (type != NodeType.OBJECT) {
                report.error(msg.message(HS_LINKS_LDO_BAD_TYPE)
                    .put("expected", NodeType.OBJECT).put("found", type));
                continue;
            }
            set = Sets.newHashSet(ldo.fieldNames());
            list = Lists.newArrayList(REQUIRED_LDO_PROPERTIES);
            list.removeAll(set);
            if (!list.isEmpty()) {
                report.error(msg.message(HS_LINKS_LDO_MISSING_REQ)
                    .put("required", REQUIRED_LDO_PROPERTIES)
                    .put("missing", list));
                continue;
            }
            if (ldo.has("targetSchema"))
                pointers.add(JsonPointer.of(keyword, index, "targetSchema"));
            checkLDO(report, ldo, msg);
        }
    }

    private void checkLDO(final ProcessingReport report, final JsonNode ldo,
        final ProcessingMessage msg)
        throws ProcessingException
    {

        JsonNode node;
        NodeType type;

        node = ldo.get("rel");
        type = NodeType.getNodeType(node);

        if (type != NodeType.STRING)
            report.error(msg.message(HS_LINKS_LDO_REL_WRONG_TYPE)
                .put("expected", NodeType.STRING).put("found", type));

        node = ldo.get("href");
        type = NodeType.getNodeType(node);

        if (type != NodeType.STRING)
            report.error(msg.message(HS_LINKS_LDO_HREF_WRONG_TYPE)
                .put("expected", NodeType.STRING).put("found", type));
        else
            try {
                new URITemplate(node.textValue());
            } catch (URITemplateParseException ignored) {
                report.error(msg.message(HS_LINKS_LDO_HREF_ILLEGAL));
            }
    }

    private ProcessingMessage newMsg(final SchemaTree tree,
        final int index)
    {
        return newMsg(tree, "").put("index", index);
    }
}
