
#Hotel Booking System


This is a description of case study of Prop-tester.

The goal of the system is to provide to user a service to reservation of hotel.

The system is constructed from four Web Services: client service (`client`), 
hotel information service (`hotel`), guest' information manager service (`guest`), and accounting service (`account`).
To easy describe their behaviors, we identify them by `c`, `h`, `g`, and `c` respectively.

For example, `!book`<sup>[c,h]</sup>(city : x<sub>0</sub>, arr : x<sub>1</sub>, nights : x<sub>2</sub>) denotes 
a sending, named `book`, realized by `client` service to `hotel` service and it has three parameters *city*, *arr* and *nights* whose values are represented by three variables x<sub>0</sub>, x<sub>1</sub>, and x<sub>2</sub> respectively.


##Services Models


The behaviors of these services are modeled as in Figure below:

![alt tag](https://github.com/nhnghia/prop-tester/raw/master/casestudy/hbs/figs/services.png)

Particularly, the `client`, on behalf of users intending to book a room, issues a *book* request to the `hotel` service with indicating  city name, arrival and number of nights, and receives `info` that contains room's information y_0 and its
price y_1.
The `client` then sends `!guest` that contains personal information of user x_3 and his (her) payment x_4 to the `account` service to book the room.
The behavior of `client` is modeled by a STGA in Figure (a).
This STGA has 4 states, each of them is attached by a set of free variables, *e.g.*, {x<sub>0</sub>, x<sub>1</sub>, x<sub>2</sub>, x<sub>3</sub>, x<sub>4</sub>} for the initial state.

The `hotel` service is modeled by STGA in Figure (b). It will propose a suitable room based on research criteria.  
After receiving a `?book` request with city y<sub>0</sub> arrival date y<sub>1</sub> and number of nights y<sub>2</sub> from `client`, it will propose a room to `client` by
sending a response `!info` containing a session identity y<sub>0</sub>, room's information x<sub>1</sub> and its price x<sub>2</sub>.
The price is calculated based on the number of nights y<sub>2</sub>.
If this number is greater or equal than a prefixed number x<sub>0</sub> then the tariff 2 is applied, otherwise the tariff is 3.
Finally, it sends `!room` to `guest` service.
 
The `guest` service to manage guests' information is modeled by STGA in Figure (c).
It recalculates the price based on information in `?room` received from `hotel`.
After receiving `? guest` from `client`, it will be forwarded this price to `account` service only if the session identities given by `hotel` and by `client` are the same.

The behavior of the last service, `account`, modeled by STGA in Figure (d), is very simple.
It does only one reception `?commit` from the `guest` service containing an amount to be withdrawn and bank card information.


## Choreography

A choreography model of HBS is depicted in as the following.

![alt tag](https://github.com/nhnghia/prop-tester/raw/master/casestudy/hbs/figs/choreography.png)

It states that the *sid* in `info`, `room` and `guest` interactions are the same, *e.g.*, they are represented by free variable x<sub>4</sub>.
It also guarantees that the *amount* in `commit` interaction issued from `guest` to `account` is the same with the *price* in
`info` responded by `hotel` to `client` service.

Let us note that the choreography model represents the required of interactions, *e.g.*, data exchange, order of execution, ...
The assignment actions are used to modify these constraints, they are not necessarily to implement explicitly by services.
For example, the *fee* responded by the `hotel` service to `client` is either 50 or 70 times of the *nights* and it is also the *amount* in `commit` interaction. This choreography model does not care about how these constraints are implemented by services.
