# FAQ
NLP project Frequently asked questions for Coinbase


# Pre requisites
  1. Install Kibana and Elastic search. Version-6.2.3

# Running instruction
  1. Start elastic search
  2. Run InitialLoader.java , it will create index for bag of words and extract features for all quesetions and index into elastic search
  3. Run Callable.java, enter you query and program will return the top 10 questions which match the query from the corpus.
