import 'package:flutter/material.dart';
import 'dart:math';

void main() {
  runApp(const SolarTechApp());
}

class SolarTechApp extends StatelessWidget {
  const SolarTechApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Solar Analysis',
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF1E1E1E),
        colorScheme: const ColorScheme.dark(primary: Colors.blueAccent),
      ),
      home: const Dashboard(),
    );
  }
}

class Dashboard extends StatefulWidget {
  const Dashboard({super.key});

  @override
  State<Dashboard> createState() => _DashboardState();
}

class _DashboardState extends State<Dashboard> {
  final tcPower = TextEditingController();
  final tcCost = TextEditingController();
  final tcS1 = TextEditingController();
  final tcS2 = TextEditingController();

  String logOutput = "";

  double erf(double x) {
    const double a1 = 0.254829592;
    const double a2 = -0.284496736;
    const double a3 = 1.421413741;
    const double a4 = -1.453152027;
    const double a5 = 1.061405429;
    const double p = 0.3275911;

    int sign = 1;
    if (x < 0) sign = -1;
    x = x.abs();

    double t = 1.0 / (1.0 + p * x);
    double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-x * x);

    return sign * y;
  }

  void loadVariant() {
    setState(() {
      tcPower.text = "5.0";
      tcCost.text = "7.0";
      tcS1.text = "1.0";
      tcS2.text = "0.25";
      logOutput = "Data loaded. Ready.";
    });
  }

  void runCalc() {
    double pc = double.tryParse(tcPower.text) ?? 0;
    double s1 = double.tryParse(tcS1.text) ?? 1;
    double s2 = double.tryParse(tcS2.text) ?? 1;
    double cost = double.tryParse(tcCost.text) ?? 0;

    double p1 = _calc(pc, s1, cost);
    double p2 = _calc(pc, s2, cost);
    double net = p2 - p1;

    setState(() {
      logOutput = """
Process initialized...
Calculating Gaussian integrals... Done.

System_1_Profit: ${p1.toStringAsFixed(2)} KUAH
System_2_Profit: ${p2.toStringAsFixed(2)} KUAH
-----------------------------------------
NET IMPROVEMENT: ${net.toStringAsFixed(2)} KUAH
""";
    });
  }

  double _calc(double pc, double sigma, double cost) {
    double delta = pc * 0.05;
    double lower = pc - delta;
    double upper = pc + delta;

    double integ = 0.5 * (erf((upper - pc) / (sigma * sqrt(2))) - erf((lower - pc) / (sigma * sqrt(2))));
    
    double w1 = pc * 24 * integ;
    double w2 = pc * 24 * (1 - integ);
    
    return ((w1 * cost * 1000) - (w2 * cost * 1000)) / 1000;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(">> SOLAR SYSTEM ANALYSIS", style: TextStyle(fontFamily: "Courier")),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              children: [
                Expanded(child: _inputBox("AVERAGE POWER (MW)", tcPower)),
                const SizedBox(width: 15),
                Expanded(child: _inputBox("ELECTRICITY COST", tcCost)),
              ],
            ),
            const SizedBox(height: 15),
            Row(
              children: [
                Expanded(child: _inputBox("SIGMA 1 (Standard)", tcS1)),
                const SizedBox(width: 15),
                Expanded(child: _inputBox("SIGMA 2 (Improved)", tcS2)),
              ],
            ),
            const SizedBox(height: 25),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: loadVariant,
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 20),
                      side: const BorderSide(color: Colors.grey),
                    ),
                    child: const Text("LOAD VARIANT", style: TextStyle(color: Colors.white70)),
                  ),
                ),
                const SizedBox(width: 15),
                Expanded(
                  child: ElevatedButton(
                    onPressed: runCalc,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blueAccent,
                      padding: const EdgeInsets.symmetric(vertical: 20),
                    ),
                    child: const Text("EXECUTE CALCULATION", style: TextStyle(color: Colors.white)),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 25),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(15),
              decoration: BoxDecoration(
                color: Colors.black26,
                border: Border.all(color: Colors.white12),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                logOutput,
                style: const TextStyle(fontFamily: "Courier", color: Colors.greenAccent, fontSize: 14),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _inputBox(String label, TextEditingController ctrl) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 11)),
        const SizedBox(height: 5),
        TextField(
          controller: ctrl,
          keyboardType: TextInputType.number,
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            filled: true,
            fillColor: const Color(0xFF2D2D2D),
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(4), borderSide: BorderSide.none),
            contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 15),
          ),
        ),
      ],
    );
  }
}