# FAQ
NLP project Frequently asked questions for Coinbase.
We implement two alogrithm to get the top 10 results which match the user query.
1. Bag of words matching
2. Hueristics based matching using features extracted from the corpus. i.e. POS tag, hypernyms, hyponyms, synonyms, dependecy graphs


# Pre requisites
  1. Install Kibana and Elastic search. Version-6.2.3

# Running instruction
  1. Start elastic search
  2. Run InitialLoader.java , it will create index for bag of words and extract features for all quesetions and index into elastic search
  3. Run Callable.java, enter you query and program will return the top 10 questions which match the query from the corpus.
