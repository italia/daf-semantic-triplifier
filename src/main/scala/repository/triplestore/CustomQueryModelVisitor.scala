package repository.triplestore

import org.openrdf.query.algebra.QueryModelVisitor

/**
 * The QueryModelVisitor could help us managing the conventional structure of a query
 */
class CustomQueryModelVisitor extends QueryModelVisitor[Exception] {

  def meet(x$1: org.openrdf.query.algebra.ZeroLengthPath): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Var): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.ListMemberOperator): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.ValueConstant): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Union): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Sum): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Str): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.StatementPattern): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Slice): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.SingletonSet): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Service): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Sample): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.SameTerm): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Regex): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Reduced): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.ProjectionElemList): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.ProjectionElem): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Projection): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.OrderElem): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Order): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Or): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Not): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Namespace): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.MultiProjection): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Move): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Modify): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Min): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Max): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.MathExpr): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.LocalName): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Load): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Like): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.LeftJoin): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.LangMatches): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Lang): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Label): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Join): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.IsURI): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.IsResource): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.IsNumeric): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.IsLiteral): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.IsBNode): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.IRIFunction): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Intersection): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.InsertData): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.In): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.If): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.GroupElem): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.GroupConcat): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Group): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.FunctionCall): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Filter): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.ExtensionElem): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Extension): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Exists): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.EmptySet): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Distinct): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Difference): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.DeleteData): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Datatype): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Create): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Count): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Copy): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.DescribeOperator): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.CompareAny): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.CompareAll): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Compare): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Coalesce): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Clear): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Bound): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.BNodeGenerator): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.BindingSetAssignment): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.Avg): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.ArbitraryLengthPath): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.And): Unit = ???

  def meet(x$1: org.openrdf.query.algebra.Add): Unit = ???
  def meet(x$1: org.openrdf.query.algebra.QueryRoot): Unit = ???
  def meetOther(x$1: org.openrdf.query.algebra.QueryModelNode): Unit = ???

}