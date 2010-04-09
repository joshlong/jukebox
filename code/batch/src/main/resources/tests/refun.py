#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import re
import sys

if __name__ == '__main__':
  tests = [
        '''
            "a1"='2'
        ''',
        '''
            "a"='1' "b"='3'
        ''',
        '''
            "josh"='awesome'
        ''',
        '''
            "josh"= "george"='of the jungle'
        ''',
        '''
        ""id"='945297' "artist"='1' "name"='Street Vibes 6 (disc 2)' "gid"='0e49cfcd-aa79-4622-9281-9f7f6360cd8b' "modpending"='1' "attributes"='{0,4,100}' "page"='693737685' "language"='120' "script"='28' "modpending_lang"= "quality"='-1' "modpending_qual"='0' "release_group"='940342'"
        '''
          ]
  tests =[a.strip() for a in tests]
  reg = '''


    (\"([^\"]*)\")=('([^']*)'|\s+)
    
  '''.strip()
  def kv(i):
      return (i[1],i[3])

  def kvs(i):
      return [kv(a) for a in i] 

  for t in tests :
      print(t)
      matches=re.findall(reg,t)
      for k,v in kvs(matches):
          print k,':',v
