package cs652.j.semantics;

import cs652.j.parser.JBaseListener;
import cs652.j.parser.JParser;
import org.antlr.symtab.*;

public class DefineScopesAndSymbols extends JBaseListener {
	public Scope currentScope;
	public static final Type JINT_TYPE = new JPrimitiveType("int");
	public static final Type JFLOAT_TYPE = new JPrimitiveType("float");
	public static final Type JSTRING_TYPE = new JPrimitiveType("string");
	public static final Type JVOID_TYPE = new JPrimitiveType("void");

	public DefineScopesAndSymbols(GlobalScope globals) {
		currentScope = globals;
	}

	@Override
	public void enterFile(JParser.FileContext ctx) {
		currentScope.define((JPrimitiveType)JINT_TYPE);
		currentScope.define((JPrimitiveType)JFLOAT_TYPE);
		currentScope.define((JPrimitiveType)JSTRING_TYPE);
		currentScope.define((JPrimitiveType)JVOID_TYPE);
		ctx.scope = (GlobalScope) currentScope;
	}

	@Override
	public void exitFile(JParser.FileContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterClassDeclaration(JParser.ClassDeclarationContext ctx) {
		JClass jClass = new JClass(ctx.name.getText(),ctx);
		jClass.setEnclosingScope(currentScope);
		jClass.setDefNode(ctx);
		currentScope.define(jClass);
		currentScope = jClass;
		ctx.scope = (JClass) currentScope;
	}

	@Override
	public void exitClassDeclaration(JParser.ClassDeclarationContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterMain(JParser.MainContext ctx) {
		JMethod jMethod = new JMethod("main",ctx);
		jMethod.setEnclosingScope(currentScope);
		jMethod.setDefNode(ctx);
		currentScope.define(jMethod);
		currentScope = jMethod;
		ctx.scope = (JMethod) currentScope;
	}

	@Override
	public void exitMain(JParser.MainContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterBlock(JParser.BlockContext ctx) {
		LocalScope localScope = new LocalScope(currentScope);
		currentScope.nest(localScope);
		currentScope = localScope;
		ctx.scope = (LocalScope) currentScope;
	}

	@Override
	public void exitBlock(JParser.BlockContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		JMethod jMethod = new JMethod(ctx.ID().getText(),ctx);
		jMethod.setEnclosingScope(currentScope);
		jMethod.setDefNode(ctx);
		currentScope.define(jMethod);
		VariableSymbol implicitThis = new VariableSymbol("this");
		implicitThis.setType((Type) currentScope);
		currentScope = jMethod;
		implicitThis.setScope(currentScope);
		currentScope.define(implicitThis);
		ctx.scope = (JMethod) currentScope;
	}

	@Override
	public void exitMethodDeclaration(JParser.MethodDeclarationContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	@Override
	public void enterFieldDeclaration(JParser.FieldDeclarationContext ctx) {
		JField jField = new JField(ctx.ID().getText());
		jField.setScope(currentScope);
		jField.setDefNode(ctx);
		Type type = (Type) currentScope.resolve(ctx.jType().getText());
		jField.setType(type);
		currentScope.define(jField);
	}

	@Override
	public void enterLocalVarStat(JParser.LocalVarStatContext ctx) {
		JVar jVar = new JVar(ctx.localVariableDeclaration().ID().getText());
		jVar.setScope(currentScope);
		jVar.setDefNode(ctx);
		Type type = (Type) currentScope.resolve(ctx.localVariableDeclaration().jType().getText());
		jVar.setType(type);
		currentScope.define(jVar);
	}

	@Override
	public void enterFormalParameter(JParser.FormalParameterContext ctx) {
		VariableSymbol param = new VariableSymbol(ctx.ID().getText());
		Type type = (Type) currentScope.resolve(ctx.jType().getText());
		param.setType(type);
		currentScope.define(param);
	}
}
