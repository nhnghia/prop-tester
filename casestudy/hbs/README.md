
Hotel Booking System
==============

This is a case study of Prop-tester.

Overview
-------

The behaviors of these services are modeled as in Figure below:

![alt tag](https://github.com/nhnghia/prop-tester/raw/master/casestudy/hbs/figs/services.png)

Particularly, the //client//, on behalf of users intending to book a
room, issues a //book// request to the //hotel// service with indicating 
city name, arrival and number of nights,
and receives //info// that contains room's information $y_0$ and its
price $y_1$.
The $\client$ then sends $!\mathtt{guest}$ that contains personal information of
user $x_3$ and his (her) payment $x_4$ to the $\account$ service to book the
room.
The behavior of $\client$ is modeled by a \ac{STGA} in
Figure~\ref{fig:stgServiceMoti}(a).
This \ac{STGA} has 4 states, each of them is attached by a set of free
variables, \eg, $\sett{x_0, x_1, x_2, x_3, x_4}$ for the initial~state.

    
The $\hotel$ service is modeled by \ac{STGA} in
Figure~\ref{fig:stgServiceMoti}(b). It will propose a suitable room based on
research criteria.  
% Its initial state has a set of two free
%variables $\sett{x_0, x_1}$.
Particularly,
after receiving a $?\mathtt{book}$ request with city $y_0$ arrival date $y_1$
and number of nights $y_2$ from $\client$, it will propose a room to $\client$ by
sending a response $!\mathtt{info}$ containing a session identity $y_0$, room's
information $x_1$ and its price $x_2$.
The price is calculated based on the number of nights $y_2$.
If this number is greater or equal than a prefixed number $x_0$ then the tariff
2 is applied, otherwise the tariff is 3.
Finally, it sends $!\mathtt{room}$ to $\guest$~service.
 
    
%
The $\guest$ service to manage guests' information is modeled by \ac{STGA} in
Figure~\ref{fig:stgServiceMoti}(c).
It recalculates the price based on information in $?\mathtt{room}$ received from
$\hotel$.
After receiving $?\mathtt{guest}$ from $\client$,
it will be forwarded this price to $\account$ service only if the session
identities given by $\hotel$ and by $\client$ are the~same.

The behavior of the last service, $\account$, modeled by \ac{STGA} in
Figure~\ref{fig:stgServiceMoti}(d), is very simple.
It does only one reception $?\mathtt{commit}$ from the $\guest$ service containing an amount to
be withdrawn and bank card information.




