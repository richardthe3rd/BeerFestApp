function cambeerfest38( jsonFile )

compact = true;
if compact
    newline = '';
    indent = '';
else
    newline = '\n';
    indent = '\t';
end

fid = fopen( jsonFile, 'wt');
fileCloser = onCleanup( @()fclose(fid) );

d = org.jsoup.Jsoup.parse( urlread('http://www.cambridgebeerfestival.com/viewnode.php?id=103') );
producers = d.select('.beerlist .producer');
producers_iterator = producers.iterator;

fprintf(fid, ['[', newline]);

while producers_iterator.hasNext
   producer = producers_iterator.next();
   producerName = toJsonString( producer.ownText() );
   producerDetails = toJsonString( producer.select('.brewerydetails').text() );
   
   products = producer.nextElementSibling().select('.product');
   products_iterator = products.iterator;
   while products_iterator.hasNext
       product = products_iterator.next();
       productName = toJsonString( product.select('.productname').text() );

       abvString = char( product.select('.abv').text() );
       productAbv = sscanf(abvString, '%f%%');
       if isempty(productAbv)
           productAbv = 0;
       end
             
       productTasting = toJsonString( product.select('.tasting').text() );
       
       fprintf(fid, ['{', newline, ...
           indent, '"brewery":{' newline, ...
           indent, indent, '"name":"%s",' newline, ...
           indent, indent, '"notes":"%s"', newline, ...
           indent, '},', newline, ...
           indent, '"name":"%s",', newline, ...
           indent, '"abv":%.1f,', newline, ...
           indent, '"notes":"%s"', newline, ...
           '}'], ...
           producerName, producerDetails, productName, productAbv, productTasting);
       
       if producers_iterator.hasNext || products_iterator.hasNext
           fprintf(fid, ',');
       end
       fprintf(fid, newline);
   end
end
fprintf(fid, [']', newline]);
end

function out = toJsonString(in)
    out = char(in); 
    out = strrep(out, '"', '\"');
end