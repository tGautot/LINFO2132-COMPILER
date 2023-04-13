package compiler.SemanticAnalyzer;

import compiler.parser.ASTNodes;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public SymbolTable prevTable = null;

    public Map<String, ASTNodes.Type> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public SymbolTable(SymbolTable prevTable) {
        table = new HashMap<>();
        this.prevTable = prevTable;
    }

    public void add(String key, ASTNodes.Type value) throws SemanticAnalyzerException {
        if (table.containsKey(key)) {
            throw new SemanticAnalyzerException("Same identifier : " + key + " for 2 differents things");
        }
        table.put(key,value);
    }

    public void update(String key, ASTNodes.Type value) throws SemanticAnalyzerException {
        SymbolTable cur = this;
        while (cur != null && !cur.table.containsKey(key)) {
            cur = cur.prevTable;
        }
        if (cur == null) {
            throw new SemanticAnalyzerException("unknown identifier : " + key);
        }

        cur.table.put(key, value);
    }

    public boolean contain(String key) throws SemanticAnalyzerException {
        SymbolTable cur = this;
        while (cur != null && !cur.table.containsKey(key)) {
            cur = cur.prevTable;
        }
        if (cur == null) {
            return false;
        }
        return true;
    }

    public ASTNodes.Type get(String key) throws SemanticAnalyzerException {
        SymbolTable cur = this;
        while (cur != null && !cur.table.containsKey(key)) {
            cur = cur.prevTable;
        }
        if (cur == null) {
            throw new SemanticAnalyzerException("unknown identifier : " + key);
        }
        return cur.table.get(key);
    }

    public ASTNodes.Type remove(String key) throws SemanticAnalyzerException {
        SymbolTable cur = this;
        while (cur != null && !cur.table.containsKey(key)) {
            cur = cur.prevTable;
        }
        if (cur == null) {
            throw new SemanticAnalyzerException("unknown identifier : " + key);
        }
        return cur.table.remove(key);
    }

}
