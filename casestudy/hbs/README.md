
# Hotel Booking System (HBS)


This is a case study of Prop-tester.

The description of the HBS can be found [here](https://github.com/nhnghia/prop-tester/blob/master/casestudy/hbs/description.md).

## Implementation

We implemented and deployed the HBS into four different clouds of RedHat, Amazon, Google, ad Heroku respectively for `client`, `hotel`, `account`, and `guest` Web services as depicted in the following Figure.
This allows to get a real distributed environment of the system, *e.g.*, there is no global clock.

<img src="https://github.com/nhnghia/prop-tester/raw/master/casestudy/hbs/figs/implementation.png" width="400px"/>

The four Web services are available at:

-  **Client**: http://hbs-nhnghia.rhcloud.com
-  **Hotel**: http://ec2-54-69-171-251.us-west-2.compute.amazonaws.com/hbs/wsdl.xml
-  **Guest**: http://hbs-guest.herokuapp.com/wsdl.xml
-  **Account**: http://hbs-accounting.appspot.com/wsdl.xml

Their execution logs are availabel at: 

-  **Client**: http://hbs-nhnghia.rhcloud.com/log-raw.txt
-  **Hotel**: http://ec2-54-69-171-251.us-west-2.compute.amazonaws.com/hbs/log-raw.txt
-  **Guest**: http://hbs-guest.herokuapp.com/log-raw.txt
-  **Account**: http://hbs-accounting.appspot.com/log?raw

## Test

