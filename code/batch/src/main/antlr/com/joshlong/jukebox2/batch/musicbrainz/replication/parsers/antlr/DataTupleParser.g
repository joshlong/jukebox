grammar DataTupleParser;

options
{
language=Java;
output=AST;
 ASTLabelType=CommonTree;
}
 
 
@lexer::header {
//ackage com.joshlong.parsers.tags.impl;
import java.util.List ; 
import java.util.ArrayList; 
}
 
@parser::header {

import java.util.List ; 
import java.util.ArrayList; 
}
 


/*  

Test Cases
	:	
	
	"album"='945920' "track"='10693637' "sequence"='2' "modpending"='0' 
	
	or
	
	"album"= "name"=
	
	or
	
	"a"='b' "a1"='b1  hello'
	
	
	
*/
EQ	:	'=';
KEY_STRING:      '"' (~'"')* '"'     ;
VALUE_STRING :	  '\'' (~'\'')* '\''  ;
TUPLE_STRING : KEY_STRING EQ VALUE_STRING;
 

//ATOMIC_STRING :  '"' (~'"')* '"'   |   ;
QUOTE 	: ('\''|'"');
SPACE 	: ' '| ' '*','' '*;
HASH_TAG:	 '#' ('a'..'z'|'A'..'Z')+   ;	
//NV_PAIR :	
//tags	 : (x=HASH_TAG { tags.add($x.text);  } 		|x=ATOMIC_STRING { tags.add($x.text);  }|x=SPACE { tags.add($x.text);  })+  ;
