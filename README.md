# GinTonic

GinTonic is an Eclipse plugin for Gin/Guice DI frameworks.
 
Project is a fork of http://www.jaculon.de/egap
 
 
## Features

* Go to binding (jump to bind point)
* Resolve implementation (jump to implementation of injected type)
* Create a provider method for an injection point
* Create the factory interface for an @Assisted annotated constructor
* Create a linked binding in a nearby Guice module
* Create the binding definition for a factory interface
* Conver an injection point to a Provider and vice versa
* Install Guice/Gin module in a parent module


## Installation
 
 Eclipse update site: 
 
     https://raw.github.com/SerCeMan/GinTonic/master/gintonic.updatesite

## Usage

* Install plugin
* Click click right mouse button on the project and select "Add GinTonic nature"
* Project should start rebuild
* Now you can achive use GinTonic plugin

### Hotkeys
* Ctrl+B  -> Go to binding:

 Set cursor to the injected field, press hotkey and you jumps to bindings of injection
* Ctrl+R  -> Resolve implementation:

 Set cursor to the injected field, press hotkey and you jumps to injected type, which should be injected at runtime
 
 
 This functional also accessible via QuickFix.
 
### Refactorings
 
QuickFix (Ctrl+1) on the code element can help you do all refactorings.
