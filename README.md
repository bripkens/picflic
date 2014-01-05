# picflic
picflic is supposed to become my image management software of choice. I do not
like to upload private images to Facebook, Goolge+ or Flickr, but I would still
like to share them. For me it is preferable to host such software myself
(basically in my basement) where I have full control over access rights and
quotas, can change the software's features as necessary and have lightning fast
access once I am home (important for large collections).

## Why I am building picflic
There are two many reasons for my interest in picflic:

 1. I need an image management solution that I can deploy on my NAS, i.e. at
    home.
 2. I need a playground for all the new and shiny technology.

The latter is especially important to me. This project is, among others, my
first "real" Clojure project and I am using it to learn more about the language.
Next up is an evaluation of web frontend architectures. picflic started out
using Facebook's React, mori and an ES 6 module transpiler for the frontend.
While this worked, I had to reinvent too many of the browsers' native
capabilities, i.e. the typical issues of the SPA approach (think routing,
scroll position handling...).
An alternative to this SPA approach would be [ROCA](http://roca-style.org/). I
am inclined to use it for this application as picflic's use cases
are easier to cover with the ROCA approach.

## What is in it for you
Should you stumble upon this, please note that this software is probably not
for you. There are more advanced (open source) solutions out there that
come with great support, mobile apps and many more features. One such
alternative would be [openphoto](http://theopenphotoproject.org/).
