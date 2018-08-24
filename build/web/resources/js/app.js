var claves;
var valores;
var chartData = [];

addChart= function(keys, values, tipo) {
    claves= keys.split(";");
    console.log(claves);
    
    valores= values.split(";");
    console.log(valores);
    
    var data = [];
    for(i=0; i<claves.length; i++) {
        data= [claves[i] , valores[i]];
        chartData.push(data);
    }
    
    chart = c3.generate({
        bindto: "#chart",
        data: {
            type: tipo,
            columns: chartData
        },
    })
}

function saludar() {
    alert('hola');
}
