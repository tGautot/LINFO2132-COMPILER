package compiler.CodeGenerator;

import compiler.SemanticAnalyzer.SemanticAnalyzerException;
import compiler.SemanticAnalyzer.SymbolTable;
import compiler.parser.ASTNodes;

import java.util.HashMap;
import java.util.Map;

public class SymbolIndexTable {

        public compiler.CodeGenerator.SymbolIndexTable prevTable = null;

        public Map<String, Pair<Integer, String>> table;

        public SymbolIndexTable() {
            table = new HashMap<>();
        }

        public SymbolIndexTable(compiler.CodeGenerator.SymbolIndexTable prevTable) {
            table = new HashMap<>();
            this.prevTable = prevTable;
        }

        public void add(String key, Integer idx, String desc) throws SemanticAnalyzerException {
            if (table.containsKey(key)) {
                throw new SemanticAnalyzerException("Same identifier : " + key + " for 2 differents things");
            }
            table.put(key,new Pair<>(idx, desc));
        }

        public void update(String key, Integer idx, String desc) throws SemanticAnalyzerException {
            compiler.CodeGenerator.SymbolIndexTable cur = this;
            while (cur != null && !cur.table.containsKey(key)) {
                cur = cur.prevTable;
            }
            if (cur == null) {
                throw new SemanticAnalyzerException("unknown identifier : " + key);
            }

            cur.table.put(key, new Pair<>(idx, desc));
        }

        public boolean contain(String key) throws SemanticAnalyzerException {
            compiler.CodeGenerator.SymbolIndexTable cur = this;
            while (cur != null && !cur.table.containsKey(key)) {
                cur = cur.prevTable;
            }
            if (cur == null) {
                return false;
            }
            return true;
        }

        public Pair<Integer, String> get(String key) throws SemanticAnalyzerException {
            compiler.CodeGenerator.SymbolIndexTable cur = this;
            while (cur != null && !cur.table.containsKey(key)) {
                cur = cur.prevTable;
            }
            if (cur == null) {
                throw new SemanticAnalyzerException("unknown identifier : " + key);
            }
            return cur.table.get(key);
        }

        public Pair<Integer, String> remove(String key) throws SemanticAnalyzerException {
            compiler.CodeGenerator.SymbolIndexTable cur = this;
            while (cur != null && !cur.table.containsKey(key)) {
                cur = cur.prevTable;
            }
            if (cur == null) {
                throw new SemanticAnalyzerException("unknown identifier : " + key);
            }
            return cur.table.remove(key);
        }



}
