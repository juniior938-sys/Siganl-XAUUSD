package com.example.data.repository

import com.example.data.model.PatternCategory
import com.example.data.model.PatternItem

object PatternDatabaseRepository {

  val patterns: List<PatternItem> = listOf(
    // --- Smart Money Concepts (SMC) ---
    PatternItem(
      id = "smc_bos",
      name = "Break of Structure (BOS)",
      category = PatternCategory.SMC,
      isBullish = true,
      summary = "Movimento que rompe uma máxima ou mínima estrutural anterior, validando a continuação da tendência institucional.",
      rules = listOf(
        "Identifique a máxima anterior em tendência de alta (ou mínima em tendência de baixa)",
        "Aguarde o fechamento com corpo de vela além do nível da máxima/mínima (não apenas pavio)",
        "Indica que grandes players continuam injetando liquidez na direção do movimento",
        "Aguarde retração em Order Block (OB) ou FVG para o gatilho de entrada"
      ),
      bestTimeframes = "H1 / M5",
      starRating = 5,
      diagramType = "BOS"
    ),
    PatternItem(
      id = "smc_ob",
      name = "Order Block (OB)",
      category = PatternCategory.SMC,
      isBullish = true,
      summary = "Última vela contrária antes de uma forte expansão institucional impulsiva. Funciona como bloco de ordens não preenchidas.",
      rules = listOf(
        "Em alta: última vela de baixa antes do rali que causou o BOS",
        "Em baixa: última vela de alta antes da queda abrupta",
        "A zona é refinada para entrada quando o preço volta para mitigar o bloco",
        "Stop Loss posicionado logo abaixo da mínima do bloco (para compras)"
      ),
      bestTimeframes = "H1 / M5",
      starRating = 5,
      diagramType = "OB"
    ),
    PatternItem(
      id = "smc_fvg",
      name = "Fair Value Gap (FVG)",
      category = PatternCategory.SMC,
      isBullish = true,
      summary = "Desequilíbrio de preço entre 3 velas consecutivas onde há pouco ou nenhum cruzamento entre pavios, criando vácuo de liquidez.",
      rules = listOf(
        "Formado pela vela 1 (máxima), vela 2 (expansão extrema), e vela 3 (mínima)",
        "O espaço livre entre a máxima da vela 1 e a mínima da vela 3 é o gap",
        "O algoritmo de entrega de preço tende a retornar para preencher 50% (CE) do FVG",
        "Excelente confluência para alvos de TP ou pontos precisos de entrada"
      ),
      bestTimeframes = "M5 / H1",
      starRating = 4,
      diagramType = "FVG"
    ),
    PatternItem(
      id = "smc_choch",
      name = "Change of Character (CHoCH)",
      category = PatternCategory.SMC,
      isBullish = true,
      summary = "Primeiro sinal técnico de que a dinâmica interna do mercado está mudando de uma tendência para outra (reversão precoce).",
      rules = listOf(
        "Ruptura da última mínima mais alta (em tendência de alta) ou máxima mais baixa (em tendência de baixa)",
        "Diferente do BOS (continuação), o CHoCH aponta esgotamento e mudança de direção",
        "Confirmação ideal após captura de liquidez (Sweep)",
        "Alvo inicial no próximo Order Block oposto"
      ),
      bestTimeframes = "H1 / M5",
      starRating = 5,
      diagramType = "CHOCH"
    ),
    PatternItem(
      id = "smc_lq",
      name = "Liquidity (LQ) & Sweeps",
      category = PatternCategory.SMC,
      isBullish = true,
      summary = "Áreas onde ordens de stop e pendentes de varejo estão concentradas (topos/fundos duplos, linhas de tendência).",
      rules = listOf(
        "Identifique topos iguais (Buy-side Liquidity) ou fundos iguais (Sell-side Liquidity)",
        "Grandes instituições induzem o varejo a comprar/vender para capturar os stops",
        "Após o 'Sweep' (varredura rápida com pavio longo), o movimento real dispara",
        "Entrada agressiva no retorno da vela para dentro do range"
      ),
      bestTimeframes = "D1 / H1 / M5",
      starRating = 5,
      diagramType = "LIQUIDITY"
    ),

    // --- Chart Patterns ---
    PatternItem(
      id = "chart_double_bottom",
      name = "Double Bottom (Fundo Duplo 'W')",
      category = PatternCategory.CHART_PATTERNS,
      isBullish = true,
      summary = "Padrão de reversão de alta formado por dois fundos consecutivos no mesmo nível de suporte seguido pelo rompimento do pescoço.",
      rules = listOf(
        "Primeiro fundo estabelece suporte após movimento de baixa",
        "Repique intermediário define a Linha de Pescoço (Neckline)",
        "Segundo fundo testa o suporte com menor volume ou divergência de RSI",
        "Entrada: Rompimento do pescoço ou reteste",
        "Projeção de TP: Distância vertical do fundo até a linha do pescoço"
      ),
      bestTimeframes = "M5 / H1",
      starRating = 5,
      diagramType = "DOUBLE_BOTTOM"
    ),
    PatternItem(
      id = "chart_head_shoulders",
      name = "Head & Shoulders (Ombro-Cabeça-Ombro)",
      category = PatternCategory.CHART_PATTERNS,
      isBullish = false,
      summary = "Padrão clássico de reversão de baixa composto por três topos, sendo o central o mais alto (cabeça) e os laterais mais baixos (ombros).",
      rules = listOf(
        "Ombro esquerdo (LS) com volume alto",
        "Cabeça (H) cria nova máxima com momentum reduzido",
        "Ombro direito (RS) não consegue romper a cabeça",
        "Gatilho no rompimento da Linha de Pescoço (Neckline)",
        "SL posicionado acima do ombro direito (RS)"
      ),
      bestTimeframes = "H1 / D1",
      starRating = 5,
      diagramType = "HEAD_SHOULDERS"
    ),
    PatternItem(
      id = "chart_bull_flag",
      name = "Bull Flag (Bandeira de Alta)",
      category = PatternCategory.CHART_PATTERNS,
      isBullish = true,
      summary = "Padrão de continuação explosiva após mastro de alta impulsivo e canal descendente estreito de consolidação.",
      rules = listOf(
        "Forte movimento de subida (Mastro) com volume expressivo",
        "Canal descendente de consolidação com volume decrescente",
        "Rompimento da LTB superior do canal com vela de força",
        "Alvo TP medido pelo tamanho integral do mastro projetado do rompimento"
      ),
      bestTimeframes = "M5 / H1",
      starRating = 5,
      diagramType = "BULL_FLAG"
    ),

    // --- Candlesticks ---
    PatternItem(
      id = "candle_bullish_engulfing",
      name = "Bullish Engulfing (Engolfo de Alta)",
      category = PatternCategory.CANDLESTICKS,
      isBullish = true,
      summary = "Vela verde de grande convicção cujo corpo engloba completamente o corpo da vela vermelha anterior em zona de suporte.",
      rules = listOf(
        "Ocorre no final de uma perna de baixa ou em reteste de Order Block",
        "Corpo da vela atual ultrapassa a abertura e o fechamento da vela anterior",
        "Volume de compra deve superar a média recente",
        "Confirmação imediata para entradas de COMPRA/BUY com SL abaixo da mínima"
      ),
      bestTimeframes = "M5 / H1",
      starRating = 5,
      diagramType = "BULLISH_ENGULFING"
    ),
    PatternItem(
      id = "candle_hammer",
      name = "Hammer (Martelo de Reversão)",
      category = PatternCategory.CANDLESTICKS,
      isBullish = true,
      summary = "Vela com corpo pequeno no topo e cauda inferior de pelo menos 2 a 3 vezes o tamanho do corpo, rejeitando preços baixos.",
      rules = listOf(
        "Aparece após tendência de baixa contínua",
        "Pavio inferior longo indica forte absorção de compra institucional",
        "Pouco ou nenhum pavio superior",
        "Entrada na superação da máxima do martelo no candle seguinte"
      ),
      bestTimeframes = "M5 / H1",
      starRating = 4,
      diagramType = "HAMMER"
    ),

    // --- Multi-Timeframe Analysis ---
    PatternItem(
      id = "mtf_alignment",
      name = "Multi-Timeframe 3-Step Alignment",
      category = PatternCategory.MULTI_TIMEFRAME,
      isBullish = true,
      summary = "Metodologia 'See the big picture, time the entry' alinhando D1, H1 e M5.",
      rules = listOf(
        "Passo 1 (HTF - D1/W1): Definir a macro tendência (Bullish/Bearish) e zonas institucionais maiores",
        "Passo 2 (MTF - H1): Confirmar a estrutura interna de mercado, identificando BOS, OB e FVG",
        "Passo 3 (LTF - M5): Gatilho de entrada por Price Action / Candlestick na zona mitigada",
        "Regra de Ouro: Nunca opere contra a tendência do H1 sem confluência de CHoCH no M5"
      ),
      bestTimeframes = "D1 -> H1 -> M5",
      starRating = 5,
      diagramType = "MTF_GUIDE"
    )
  )

  fun getPatternsByCategory(category: PatternCategory): List<PatternItem> {
    return patterns.filter { it.category == category }
  }

  fun search(query: String): List<PatternItem> {
    if (query.isBlank()) return patterns
    val q = query.trim().lowercase()
    return patterns.filter {
      it.name.lowercase().contains(q) ||
        it.summary.lowercase().contains(q) ||
        it.rules.any { r -> r.lowercase().contains(q) }
    }
  }
}
