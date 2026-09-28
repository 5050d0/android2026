# Introduction
## Hello, world!
``` kotlin
fun start(): String = "OK"
```
## Named arguments
``` kotlin
fun joinOptions(options: Collection<String>) =
        options.joinToString(
            prefix = "[",
            separator = ", ",
        	postfix = "]")
```
## Default arguments
``` kotlin
fun foo(name: String, number: Int = 42, toUpperCase: Boolean = false) =
        (if (toUpperCase) name.uppercase() else name) + number

fun useFoo() = listOf(
        foo("a"),
        foo("b", number = 1),
        foo("c", toUpperCase = true),
        foo(name = "d", number = 2, toUpperCase = true)
)
```
## [Triple-quoted strings](https://play.kotlinlang.org/koans/Introduction/Triple-quoted%20strings/Task.kt)
``` kotlin
const val question = "life, the universe, and everything"
const val answer = 42

val tripleQuotedString = """
    #question = "$question"
    #answer = $answer""".trimMargin("#")

fun main() {
    println(tripleQuotedString)
}
```

## [String templates](https://play.kotlinlang.org/koans/Introduction/String%20templates/Task.kt)
```
val month = "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)"

fun getPattern(): String = """\d{2} """+month+""" \d{4}"""
```
## [Nullable types](https://play.kotlinlang.org/koans/Introduction/Nullable%20types/Task.kt)
``` kotlin
fun sendMessageToClient(
        client: Client?, message: String?, mailer: Mailer
) {
     

    val personalInfo = client?.personalInfo;

    val email = personalInfo?.email;
	if (email != null && message != null){
    	mailer.sendMessage(email, message);}
}

class Client(val personalInfo: PersonalInfo?)
class PersonalInfo(val email: String?)
interface Mailer {
    fun sendMessage(email: String, message: String)
}
```
## [Nothing type](https://play.kotlinlang.org/koans/Introduction/Nothing%20type/Task.kt)
``` kotlin
import java.lang.IllegalArgumentException

fun failWithWrongAge(age: Int?): Nothing {
    throw IllegalArgumentException("Wrong age: $age")
}

fun checkAge(age: Int?) {
    if (age == null || age !in 0..150) failWithWrongAge(age)
    println("Congrats! Next year you'll be ${age + 1}.")
}

fun main() {
    checkAge(10)
}
```
## [Lambdas](https://play.kotlinlang.org/koans/Introduction/Lambdas/Task.kt)
``` kotlin
fun containsEven(collection: Collection<Int>): Boolean =
        collection.any { x -> x%2==0 }
```

# Classes
## [Data classes](https://play.kotlinlang.org/koans/Classes/Data%20classes/Task.kt)
``` kotlin
data class Person (val name: String, val age: Int)

fun getPeople(): List<Person> {
    return listOf(Person("Alice", 29), Person("Bob", 31))
}

fun comparePeople(): Boolean {
    val p1 = Person("Alice", 29)
    val p2 = Person("Alice", 29)
    return p1 == p2  // should be true
}
```
## [Smart casts](https://play.kotlinlang.org/koans/Classes/Smart%20casts/Task.kt)
``` kotlin
fun eval(expr: Expr): Int =
        when (expr) {
            is Num -> return expr.value
            is Sum -> return eval(expr.left) + eval(expr.right)
            else -> throw IllegalArgumentException("Unknown expression")
        }

interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
```
## [Sealed classes](https://play.kotlinlang.org/koans/Classes/Sealed%20classes/Task.kt)
``` kotlin
fun eval(expr: Expr): Int =
        when (expr) {
            is Num -> return expr.value
            is Sum -> return eval(expr.left) + eval(expr.right)
        }

sealed interface Expr
class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr
```
## [Rename on import](https://play.kotlinlang.org/koans/Classes/Rename%20on%20import/Task.kt)
``` kotlin
 import kotlin.random.Random as KRandom
import java.util.Random as JRandom

fun useDifferentRandomClasses(): String {
    return "Kotlin random: " +
            KRandom.nextInt(2) +
            " Java random:" +
            JRandom().nextInt(2) +
            "."
}
```
## [Extension functions](https://play.kotlinlang.org/koans/Classes/Extension%20functions/Task.kt)
``` kotlin
fun Int.r(): RationalNumber = RationalNumber(this, 1)

fun Pair<Int, Int>.r(): RationalNumber = RationalNumber(this.first, this.second)

data class RationalNumber(val numerator: Int, val denominator: Int)
```

# [Conventions](https://play.kotlinlang.org/koans/Conventions/Comparison/Task.kt)
## [Comparison](https://play.kotlinlang.org/koans/Conventions/Comparison/Task.kt)
``` kotlin
data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<MyDate> {
    override fun compareTo(other: MyDate) : Int {
        return this.year*365 - other.year*365 + this.month*30 - other.month*30 + this.dayOfMonth - other.dayOfMonth
    }
}

fun test(date1: MyDate, date2: MyDate) {
    // this code should compile:
    println(date1 < date2)
}
```
## [Ranges](https://play.kotlinlang.org/koans/Conventions/Ranges/Task.kt)
``` kotlin
fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
    return (first < date) && (date < last)
}
```
## [For loop](https://play.kotlinlang.org/koans/Conventions/For%20loop/Task.kt)
```kotlin
class DateRange(val start: MyDate, val end: MyDate) : Iterable<MyDate>{
	override fun iterator(): Iterator<MyDate> =
        object : Iterator<MyDate> {
            private var current = start
            override fun hasNext(): Boolean = current <= end
            override fun next(): MyDate {
            	if (!hasNext()) throw NoSuchElementException()
            	val result = current
            	current = current.followingDate()
            	return result
            }
          }
}

fun iterateOverDateRange(firstDate: MyDate, secondDate: MyDate, handler: (MyDate) -> Unit) {
    for (date in firstDate..secondDate) {
        handler(date)
    }
}
```
## [Operators overloading](https://play.kotlinlang.org/koans/Conventions/Operators%20overloading/Task.kt)
```kotlin
import TimeInterval.*

data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int)

// Supported intervals that might be added to dates:
enum class TimeInterval { DAY, WEEK, YEAR }

operator fun MyDate.plus(timeInterval: TimeInterval)= 
    addTimeIntervals(timeInterval, 1)

operator fun MyDate.plus(longtimeInterval: LongTimeInterval) = 
    addTimeIntervals(longtimeInterval.interval, longtimeInterval.n)

class LongTimeInterval(val interval: TimeInterval, val n: Int)
operator fun TimeInterval.times(n: Int) =
    LongTimeInterval(this, n)
fun task1(today: MyDate): MyDate {
    return today + YEAR + WEEK
}

fun task2(today: MyDate): MyDate {
    return today + YEAR * 2 + WEEK * 3 + DAY * 5
}
```
## [Invoke](https://play.kotlinlang.org/koans/Conventions/Invoke/Task.kt)
``` kotlin
class Invokable {
    var numberOfInvocations: Int = 0
        private set

    operator fun invoke(): Invokable {
        numberOfInvocations ++
        return this
        
    }
}

fun invokeTwice(invokable: Invokable) = invokable()()
```
# [Collections](https://play.kotlinlang.org/koans/Collections/Introduction/Task.kt)
## [Introduction](https://play.kotlinlang.org/koans/Collections/Introduction/Task.kt)
```kotlin
fun Shop.getSetOfCustomers(): Set<Customer> =
        customers.toSet()
```
## [Sort](https://play.kotlinlang.org/koans/Collections/Sort/Task.kt)
``` kotlin
// Return a list of customers, sorted in the descending by number of orders they have made
fun Shop.getCustomersSortedByOrders(): List<Customer> =
        customers.sortedByDescending{it.orders.size}
```
## [Filter map](https://play.kotlinlang.org/koans/Collections/Filter%20map/Task.kt)
``` kotlin
// Find all the different cities the customers are from
fun Shop.getCustomerCities(): Set<City> =
        (customers.map {it.city}).toSet()

// Find the customers living in a given city
fun Shop.getCustomersFrom(city: City): List<Customer> =
        customers.filter {it.city==city}
```
## [All Any and other predicates](https://play.kotlinlang.org/koans/Collections/All%20Any%20and%20other%20predicates/Task.kt)
``` kotlin
// Return true if all customers are from a given city
fun Shop.checkAllCustomersAreFrom(city: City): Boolean =
        customers.all {it.city == city}

// Return true if there is at least one customer from a given city
fun Shop.hasCustomerFrom(city: City): Boolean =
        customers.any {it.city == city}

// Return the number of customers from a given city
fun Shop.countCustomersFrom(city: City): Int =
        customers.count {it.city == city}

// Return a customer who lives in a given city, or null if there is none
fun Shop.findCustomerFrom(city: City): Customer? =
        customers.find {it.city == city}
```
## [Associate](https://play.kotlinlang.org/koans/Collections/Associate/Task.kt)
```kotlin
// Build a map from the customer name to the customer
fun Shop.nameToCustomerMap(): Map<String, Customer> =
       	customers.associateBy {it.name}

// Build a map from the customer to their city
fun Shop.customerToCityMap(): Map<Customer, City> =
        customers.associateWith {it.city}

// Build a map from the customer name to their city
fun Shop.customerNameToCityMap(): Map<String, City> =
        customers.associate {it.name to it.city}
```
## [GroupBy](https://play.kotlinlang.org/koans/Collections/GroupBy/Task.kt)
```kotlin
fun Shop.groupCustomersByCity(): Map<City, List<Customer>> =
        customers.groupBy{it.city}
```
## [Partition](https://play.kotlinlang.org/koans/Collections/Partition/Task.kt)
``` kotlin
// Return customers who have more undelivered orders than delivered
fun Shop.getCustomersWithMoreUndeliveredOrders(): Set<Customer> = customers.filter {
    val (delivered, undelivered) = it.orders.partition { it.isDelivered }
    undelivered.size > delivered.size
}.toSet()

```
## [FlatMap](https://play.kotlinlang.org/koans/Collections/FlatMap/Task.kt)
```kotlin
// Return all products the given customer has ordered
fun Customer.getOrderedProducts(): List<Product> =
        orders.flatMap {it.products}

// Return all products that were ordered by at least one customer
fun Shop.getOrderedProducts(): Set<Product> =
        customers.flatMap{it.getOrderedProducts()}.toSet()
```
## [Max min](https://play.kotlinlang.org/koans/Collections/Max%20min/Task.kt)
``` kotlin
// Return a customer who has placed the maximum amount of orders
fun Shop.getCustomerWithMaxOrders(): Customer? =
        customers.maxByOrNull{it.orders.size}
// Return the most expensive product that has been ordered by the given customer
fun getMostExpensiveProductBy(customer: Customer): Product? =
        customer.orders.flatMap{it.products}.maxByOrNull{it.price}
```
## [Sum](https://play.kotlinlang.org/koans/Collections/Sum/Task.kt)
``` kotlin
// Return the sum of prices for all the products ordered by a given customer
fun moneySpentBy(customer: Customer): Double =
        customer.orders.flatMap{it.products}.sumOf{it.price}
```
## [Fold and reduce](https://play.kotlinlang.org/koans/Collections/Fold%20and%20reduce/Task.kt)
``` kotlin
// Return the set of products that were ordered by all customers
fun Shop.getProductsOrderedByAll(): Set<Product> =
    this.customers.map(Customer::getOrderedProducts).reduce{
        result, customer -> result.intersect(customer)
    }

fun Customer.getOrderedProducts(): Set<Product> =
    this.orders.flatMap{it.products}.toSet()
```
## [Compound tasks](https://play.kotlinlang.org/koans/Collections/Compound%20tasks/Task.kt)
``` kotlin
// Find the most expensive product among all the delivered products
// ordered by the customer. Use `Order.isDelivered` flag.
fun findMostExpensiveProductBy(customer: Customer): Product? {
    return customer
        .orders
        .filter{it.isDelivered}
        .flatMap{it.products}
        .maxByOrNull{it.price}
}

// Count the amount of times a product was ordered.
// Note that a customer may order the same product several times.
fun Shop.getNumberOfTimesProductWasOrdered(product: Product): Int = this.customers.flatMap{
    it.getOrderedProducts()
}.count { it == product}

fun Customer.getOrderedProducts(): List<Product> =
        this.orders.flatMap{ it.products}
```
# [Properties](https://play.kotlinlang.org/koans/Properties/Properties/Task.kt)
## [Properties](https://play.kotlinlang.org/koans/Properties/Properties/Task.kt)
```kotlin
class PropertyExample() {
    var counter = 0
    var propertyWithCounter: Int? = null
        set(v) {
            field = v
            counter++
        }
}
```
## [Lazy property](https://play.kotlinlang.org/koans/Properties/Lazy%20property/Task.kt)
```kotlin
class LazyProperty(val initializer: () -> Int) {
    var value: Int? = null
    val lazy: Int
        get() {
            if (value==null){
                value = initializer()
            }
            return value!!
        }
}
```
## [Delegates examples](https://play.kotlinlang.org/koans/Properties/Delegates%20examples/Task.kt)
```kotlin
class LazyProperty(val initializer: () -> Int) {
    val lazyValue: Int by lazy(initializer)
}
```
## [Delegates how it works](https://play.kotlinlang.org/koans/Properties/Delegates%20how%20it%20works/Task.kt)
``` kotlin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class D {
    var date: MyDate by EffectiveDate()
}

class EffectiveDate<R> : ReadWriteProperty<R, MyDate> {

    var timeInMillis: Long? = null

    override fun getValue(thisRef: R, property: KProperty<*>): MyDate {
        return timeInMillis!!.toDate()
        
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: MyDate) {
        timeInMillis = value.toMillis()
    }
}
```
