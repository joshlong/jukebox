grammar DataTupleParser;

options
{
language=Java;
output=AST;
 ASTLabelType=CommonTree;
}
@lexer::members {    

 }

 

@parser::members {

} 
 
@lexer::header {
package com.joshlong.jukebox2.batch.musicbrainz.replication.parsers.antlr;
import java.util.List ; 
import java.util.ArrayList; 
}
 
@parser::header {
package com.joshlong.jukebox2.batch.musicbrainz.replication.parsers.antlr;
import java.util.List ; 
import java.util.ArrayList; 
}
 



SPACE 	: ' '| ' '*','' '*;
EQ	:	'=';
KEY_STRING:      '"' (~'"')* '"'     ;
VALUE_STRING :	  '\'' (~'\'')* '\'' | SPACE+ ;
TUPLE_STRING : (KEY_STRING EQ VALUE_STRING)+;
 

//ATOMIC_STRING :  '"' (~'"')* '"'   |   ;
QUOTE 	: ('\''|'"');

HASH_TAG:	 '#' ('a'..'z'|'A'..'Z')+   ;	
//NV_PAIR :	
//tags	 : (x=HASH_TAG { tags.add($x.text);  } 		|x=ATOMIC_STRING { tags.add($x.text);  }|x=SPACE { tags.add($x.text);  })+  ;
tuples	:	TUPLE_STRING;
