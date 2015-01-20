package org.mousehole

import akka.actor.ActorSystem

/**
 * Created by ruguer on 1/19/15.
 */
object Test1 {
  def main(args:Array[String]) : Unit = {
    val system = ActorSystem("bravo")

    val int2String = (x:(Int,Int)) => x._1.toString

    val x = Instrumentation[(Int,Int)](system)

    val i = (for {i <- 1 to 50} yield (i,i) ).iterator

    val f1 = x.instrument[(Int,Int)]("square",int2String ,(x:(Int,Int)) => (x._1,x._2 ^ 2))

    val f2 = x.instrument[(Int,Int)]("add20","square",int2String,(x:(Int,Int)) => (x._1,x._2 + 20))

    val f3 = x.instrument[(Int,Int)]("mod14","add20",int2String,(x:(Int,Int)) => (x._1,x._2 % 14))

    val f4 = x.instrument[(Int,Int)]("identity","mod14",int2String,(t: (Int,Int)) => t)

    i.map(f1).map(f2).map(f3).map(f4).foldLeft(0){(acc,m) =>
      val y =  x.instrument[(Int,Int)]("sink","identity",int2String,(t:(Int,Int)) => (t._1,t._2+acc))
      y(m)._2
    }
  }
}
