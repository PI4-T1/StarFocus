package br.edu.puccampinas.starfocusapp

class ProgressReport {
}

//fun getTarefasDoMes(dataTarefas: Map<String, List<Map<String, Any>>>, mesAno: String): List<List<Tarefa>> {
//    val tarefasDoMes = MutableList(31) { mutableListOf<Tarefa>() }
//
//    dataTarefas.forEach { (data, tarefasList) ->
//        if (data.substring(3, 10) == mesAno) { // Verifica se a data pertence ao mês/ano especificado (formato dd-MM-yyyy)
//            val dia = data.substring(0, 2).toInt() // Extrai o dia da data
//            val tarefas = tarefasList.mapNotNull { tarefa ->
//                val id = tarefa["id"] as? String
//                val texto = tarefa["texto"] as? String
//                val status = tarefa["status"] as? String ?: "Pendente"
//                if (id != null && texto != null) Tarefa(id, texto, status) else null
//            }
//            tarefasDoMes[dia - 1] = tarefas.toMutableList() // Adiciona as tarefas no índice correspondente ao dia
//        }
//    }
//    return tarefasDoMes
//}
//
//data class Tarefa(val id: String, val texto: String, val status: String)