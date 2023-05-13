<p align="center"><img width="300" src="https://user-images.githubusercontent.com/2738294/235072055-603950db-78b3-4011-8a4e-2f870783f423.png" /></p>

# MetaTerm

An open source terminology management solution. The main goal of this app is to help translators to manage their glossaries in a structured but at the same time highly configurable way. Termbases are a highly valuable resource and require time to be built and validated, they are key for professionals to adapt to their customers' and domain requirements while ensuring consistency and high quality. One of the main concerns of my approach to terminology management is that one's data must always reside on one's own machine, so terminological entries must be stored in a *local* database and be never sent over the network.

## Rationale

Once upon a time, when my career as a software developer had not started yet, I happened to work in the field of professional translation. At that time, no free and open source solutions existed, the only tools that were available were expensive and hard to use.
Here is an attempt to provide an alternate solution as free software. This is still mostly work in progress, so feel free to experiment, report bugs and suggest possible improvements. This project is developed in my free time, so please be patient.

## Functions
MetaTerm is intended as a concept-oriented terminology management tool, allowing glossary makers to create terminological entries for concepts (rather than for terms), each of which can have multiple lemmata for multiple languages (when more terms for the same language coexist in the same entries they are to be considered alternatives or parasynonyms).
Each entry, language or term within a language can be described by one or more properties, whose name, level and type can be defined for each termbase in the definition model, in order to guarantee a flexible structure depending on the domain or the user's requirements.
The structure of the form for term insertion can be customized as well, as it is referred to as the input model (configurable for each termbase too).
Entries can be filtered according to a fuzzy or exact match on their descriptive fields or lemmata. Fields that are marked for inclusion in search are fuzzily matched against the content of the search field in the main toolbar.

### Entry management

Entry browsing
<img width="1552" alt="main_screen" src="https://user-images.githubusercontent.com/2738294/234402080-8a4963a3-3d86-4695-92dc-cf680a06f8ca.png">

New entry
<img width="1552" alt="new_entry" src="https://user-images.githubusercontent.com/2738294/234402245-c77d4100-6d8b-4716-ad9d-2c29f0e8e913.png">

Edit entry
<img width="1552" alt="edit_entry" src="https://user-images.githubusercontent.com/2738294/234402265-a7f3a72a-8d07-40af-8383-9b8e26deca5c.png">

Filtering
<img width="912" alt="dialog_filter" src="https://user-images.githubusercontent.com/2738294/234403179-4a0e59fb-3baf-49ec-a8db-f9b99ae6f7c4.png">


### Statistics

<img width="712" alt="dialog_statistics" src="https://user-images.githubusercontent.com/2738294/234402313-fea6f748-52b0-46ba-9c9b-14eb562cd42c.png">

### Termbase management

Browse existing termbases
<img width="712" alt="dialog_manage" src="https://user-images.githubusercontent.com/2738294/234402369-c2a46c68-c47a-4156-9202-548e7e8fa09c.png">

Create a new termbase
<img width="912" alt="create_step_2" src="https://user-images.githubusercontent.com/2738294/234402833-f97074da-2018-4b28-a0dd-d6c833074a98.png">
<img width="912" alt="create_step_1" src="https://user-images.githubusercontent.com/2738294/234402814-4b2d223c-8782-439d-9cf0-ae7aff66ce34.png">
<img width="912" alt="create_step_3" src="https://user-images.githubusercontent.com/2738294/234402854-8c8fb36f-214d-4749-be27-c5396722fcd4.png">

Edit termbase
<img width="912" alt="dialog_edit" src="https://user-images.githubusercontent.com/2738294/234402945-71c5e34f-b478-4660-bcdb-e00743c354ba.png">

## Technologies

This project was also created as a playground to experiment with some frameworks and libraries I wanted to learn (and play a little with). It is a pure Kotlin project taking advantage of the **Kotlin multiplatform** technology, even though no native code was needed and it's all in the JVM flavour.
The UI layer is written in **Jetpack Compose**, using the desktop porting of the library which is still experimental (but reliable though).

In order to have state and instance retention (ViewModels) in a desktop/multiplatform environment, I chose the **Decompose** library or at least, to keep things as simple as possible, the smallest subset of its functionalities that I needed, e.g. instance retention.  Dependency injection is obtained through the **Koin** framework, but as a thin layer with no annotation processing and other bells and whistles.

The persistence layer was written with JetBrains **Exposed** ORM library, combined with the JDBC driver and an embedded **H2 database**, since it was crucial to me to keep data within the user's local machine.

Since this is an open field for experimentation, I am willing to accept change proposals/suggestions and even to rewrite entire layers of the application architecture just for fun. During development I have already switched the following technologies:
- SQLite ➞ H2 for persistence
- Kodein ➞ Koin for DI
- Precompose ➞ Decompose for MVVM and navigation

## Trivia

The name "MetaTerm" is a pun between the name of well known commercial solutions and the Greek verb μεταφράζω meaning "to translate". This is part of a larger set of translation tools that I am creating in my spare time. If you like the idea, please check out [MetaLine](https://github.com/diegoberaldin/MetaLine) which is used for text alignment to create translation memories in TMX.
