# WordMatch
An Android app to demonstrate fast text searching through the use of virtual SQLite tables

keywords.txt contains the following entries:
"5 star", "aashirwad", "aata", "agarbatti", "ajwain", "allout", "almond", "aluminium", "amla", "amul", "amul butter", "amul cheese", 
"amul ghee", "amul milk", "apple", "arhar", "ariel", "aashirvaad", "atta", "axe", "ayur", "baby powder", "badam", "bambino", "banana", 
"basmati", "bathroom", "beans", "besan", "bhel", "bhujia", "bingo", "biotique", "biscuit", "black", "body lotion", "body wash", "boondi", 
"boost", "bottle", "bourbon", "bournvita", "bread", "Britannia", "brook", "broom", "brown", "brush", "butter", "cadbury", "cake", 
"candle", "capsicum", "carrot", "cashew", "catch", "cauliflower", "cerelac", "chakra", "chana", "chana dal", etc...

The app loads these entries into a virtual table on application launch.
When the user enters text, a FTS (fast text search) is performed and the "words matching from the beginning" are displayed to the user.
