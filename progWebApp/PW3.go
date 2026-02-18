package main

import (
	"fmt"
	"html/template"
	"math"
	"net/http"
	"strconv"
)

type EnergySystem struct {
	Power    float64
	Sigma1   float64
	Sigma2   float64
	Cost     float64
}

type CalculationResult struct {
	Profit1  float64
	Profit2  float64
	NetGain  float64
	InputMap map[string]string
}

func main() {
	http.HandleFunc("/", renderUI)
	http.HandleFunc("/calculate", handleCalculation)
	fmt.Println("Server running on port 9999...")
	http.ListenAndServe(":9999", nil)
}

func renderUI(w http.ResponseWriter, r *http.Request) {
	tmpl, _ := template.New("ui").Parse(htmlTemplate)
	tmpl.Execute(w, nil)
}

func handleCalculation(w http.ResponseWriter, r *http.Request) {
	sys := EnergySystem{
		Power:  parse(r.FormValue("power")),
		Sigma1: parse(r.FormValue("sigma1")),
		Sigma2: parse(r.FormValue("sigma2")),
		Cost:   parse(r.FormValue("cost")),
	}

	p1 := calculateProfit(sys.Power, sys.Sigma1, sys.Cost)
	p2 := calculateProfit(sys.Power, sys.Sigma2, sys.Cost)

	inputs := make(map[string]string)
	for k, v := range r.Form {
		inputs[k] = v[0]
	}

	res := CalculationResult{
		Profit1:  p1,
		Profit2:  p2,
		NetGain:  p2 - p1,
		InputMap: inputs,
	}

	tmpl, _ := template.New("ui").Parse(htmlTemplate)
	tmpl.Execute(w, res)
}

func calculateProfit(pc, sigma, cost float64) float64 {
	rangeVal := pc * 0.05
	p1 := pc - rangeVal
	p2 := pc + rangeVal

	share := 0.5 * (math.Erf((p2-pc)/(sigma*math.Sqrt(2))) - math.Erf((p1-pc)/(sigma*math.Sqrt(2))))
	
	totalEnergy := pc * 24
	w1 := totalEnergy * share
	w2 := totalEnergy * (1 - share)

	profit := (w1 * cost) - (w2 * cost)
	return profit 
}

func parse(s string) float64 {
	v, _ := strconv.ParseFloat(s, 64)
	return v
}

const htmlTemplate = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>PV_PROFIT_CALC [VAR_6]</title>
    <style>
        body {
            background-color: #1e1e1e;
            color: #d4d4d4;
            font-family: 'Consolas', 'Monaco', monospace;
            display: flex;
            justify-content: center;
            padding-top: 40px;
        }
        .main-container {
            width: 700px;
            background-color: #252526;
            padding: 25px;
            border: 1px solid #3e3e42;
            box-shadow: 0 10px 20px rgba(0,0,0,0.5);
        }
        h2 {
            color: #4ec9b0;
            border-bottom: 2px solid #3e3e42;
            padding-bottom: 10px;
            margin-top: 0;
        }
        .input-group {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        .field-box {
            background-color: #333333;
            padding: 15px;
            border-left: 3px solid #007acc;
        }
        label {
            display: block;
            font-size: 0.8em;
            color: #9cdcfe;
            margin-bottom: 8px;
        }
        input {
            width: 90%;
            background-color: #1e1e1e;
            border: 1px solid #3e3e42;
            color: #ce9178;
            padding: 8px;
            font-family: inherit;
            font-size: 1em;
        }
        input:focus {
            outline: none;
            border-color: #007acc;
        }
        .btn-panel {
            display: flex;
            gap: 15px;
            margin-top: 20px;
        }
        button {
            flex: 1;
            padding: 12px;
            border: none;
            font-family: inherit;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-calc {
            background-color: #007acc;
            color: white;
        }
        .btn-calc:hover { background-color: #005a9e; }
        .btn-auto {
            background-color: #3e3e42;
            color: #dcdcaa;
            border: 1px solid #555;
        }
        .btn-auto:hover { background-color: #4e4e52; }

        .console-output {
            margin-top: 25px;
            background-color: #101010;
            border: 1px solid #444;
            padding: 15px;
            font-size: 0.9em;
        }
        .log-entry { margin-bottom: 5px; }
        .success { color: #b5cea8; }
        .highlight { color: #569cd6; }
        .total { 
            margin-top: 10px; 
            padding-top: 10px; 
            border-top: 1px dashed #666; 
            font-weight: bold; 
            color: #ce9178; 
        }
    </style>
    <script>
        function setVariant6() {
            document.getElementById('power').value = "5.0";
            document.getElementById('sigma1').value = "1.0";
            document.getElementById('sigma2').value = "0.25";
            document.getElementById('cost').value = "7.0";
        }
    </script>
</head>
<body>
    <div class="main-container">
        <h2>>> SOLAR SYSTEM ANALYSIS</h2>
        <form action="/calculate" method="POST">
            <div class="input-group">
                <div class="field-box">
                    <label>AVERAGE_POWER (MW)</label>
                    <input type="text" id="power" name="power" value="{{.InputMap.power}}">
                </div>
                <div class="field-box">
                    <label>ELECTRICITY COST (UAH/kWh)</label>
                    <input type="text" id="cost" name="cost" value="{{.InputMap.cost}}">
                </div>
                <div class="field-box">
                    <label>SIGMA ERROR 1 (Standard)</label>
                    <input type="text" id="sigma1" name="sigma1" value="{{.InputMap.sigma1}}">
                </div>
                <div class="field-box">
                    <label>SIGMA ERROR 2 (Improved)</label>
                    <input type="text" id="sigma2" name="sigma2" value="{{.InputMap.sigma2}}">
                </div>
            </div>

            <div class="btn-panel">
                <button type="button" class="btn-auto" onclick="setVariant6()">LOAD VARIANT 6</button>
                <button type="submit" class="btn-calc">EXECUTE CALCULATION</button>
            </div>
        </form>

        {{if .Profit1}}
        <div class="console-output">
            <div class="log-entry">Process initialized...</div>
            <div class="log-entry">Calculating Gaussian integrals... <span class="success">Done.</span></div>
            <br>
            <div class="log-entry">System_1_Profit: <span class="highlight">{{printf "%.2f" .Profit1}} kUAH</span></div>
            <div class="log-entry">System_2_Profit: <span class="highlight">{{printf "%.2f" .Profit2}} kUAH</span></div>
            <div class="total">NET IMPROVEMENT: {{printf "%.2f" .NetGain}} kUAH</div>
        </div>
        {{end}}
    </div>
</body>
</html>
`