function html2json( url, jsonFile, compact )
% HTML2JSON Scrape the Cambridge Beer festival website
%
% html2json( URL, JSONFILE ) scrapes the page at URL and produces JSONFILE.
%

if nargin < 3
    compact = true;
end

if nargin < 2 || strcmp(jsonFile, '-')
    fid = 1;
else
    fid = fopen( jsonFile, 'wt');
    fileCloser = onCleanup( @()fclose(fid) );
end

if compact
    newline = '';
    indent = '';
else
    newline = '\n';
    indent = '\t';
end


if exist('org.jsoup.Jsoup', 'class')  ~= 8
    jsoup_jar_path = fullfile( fileparts(mfilename), 'jsoup-1.5.2.jar' );
    javaaddpath(jsoup_jar_path);
end

d = org.jsoup.Jsoup.parse( urlread(url) );
producers = d.select('.beerlist .producer');
producers_iterator = producers.iterator;

fprintf(fid, ['{', newline]);
fprintf(fid, [indent, '"producers": [', newline]);
while producers_iterator.hasNext
    printProducer(fid, producers_iterator.next(), indent, newline);    
    if producers_iterator.hasNext
        fprintf(fid, ',');
    end
    fprintf(fid, newline);
end
fprintf(fid, [indent, ']', newline]);

fprintf(fid, ['}', newline]);
end

function printProducer(fid, producer, indent, newline)
producerName = toJsonString( producer.ownText() );
producerDetails = toJsonString( producer.select('.brewerydetails').text() );
products = producer.nextElementSibling().select('.product');
products_iterator = products.iterator;

fprintf(fid, [indent, indent, '{',  newline]);
fprintf(fid, [indent, indent, indent, '"id": "%s", ', newline], producerName);
fprintf(fid, [indent, indent, indent, '"name": "%s", ', newline], producerName);
fprintf(fid, [indent, indent, indent, '"notes": "%s", ', newline], producerDetails);
fprintf(fid, [indent, indent, indent, '"produce": [',  newline]);
while products_iterator.hasNext
    printProduct(fid, products_iterator.next(), indent, newline);
    if products_iterator.hasNext
        fprintf(fid, ',');
    end
    fprintf(fid, newline);
end
fprintf(fid, [indent, indent, indent, ']',  newline]);
fprintf(fid, [indent, indent, '}']);
end

function printProduct(fid, product, indent, newline)
productName = toJsonString( product.select('.productname').text() );

abvString = char( product.select('.abv').text() );
productAbv = sscanf(abvString, '%f%%');
if isempty(productAbv)
    productAbv = 0;
else
    productAbv = productAbv(1);
end

productTasting = toJsonString( product.select('.tasting').text() );

fprintf(fid, [indent, indent, indent, '{',  newline]);
fprintf(fid, [indent, indent, indent, indent, '"id": "%s", ', newline], productName);
fprintf(fid, [indent, indent, indent, indent, '"name": "%s", ', newline], productName);
fprintf(fid, [indent, indent, indent, indent, '"abv": %.1f, ', newline], productAbv);
fprintf(fid, [indent, indent, indent, indent, '"notes": "%s" ', newline], productTasting);
fprintf(fid, [indent, indent, indent, '}']);

end

function out = toJsonString(in)
out = char(in);
out = strrep(out, '"', '\"');
end
