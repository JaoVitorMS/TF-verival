MAIN := relatorio
BUILD_DIR := build
PDFLATEX := pdflatex
LATEXFLAGS := -interaction=nonstopmode -halt-on-error -file-line-error -output-directory=$(BUILD_DIR)

# Opcional: sobrescreva os nomes pelo terminal.
# Exemplo:
# make pdf PESSOA1="Ana Silva" PESSOA2="Bruno Lima" PESSOA3="Carla Souza"
PESSOA1 ?=
PESSOA2 ?=
PESSOA3 ?=

DEFS :=
ifneq ($(strip $(PESSOA1)),)
DEFS += \def\PessoaUm{$(PESSOA1)}
endif
ifneq ($(strip $(PESSOA2)),)
DEFS += \def\PessoaDois{$(PESSOA2)}
endif
ifneq ($(strip $(PESSOA3)),)
DEFS += \def\PessoaTres{$(PESSOA3)}
endif

.PHONY: all pdf clean clean-all view

all: pdf

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

pdf: $(BUILD_DIR)
	$(PDFLATEX) $(LATEXFLAGS) '$(DEFS)\input{$(MAIN).tex}'
	$(PDFLATEX) $(LATEXFLAGS) '$(DEFS)\input{$(MAIN).tex}'
	cp $(BUILD_DIR)/$(MAIN).pdf $(MAIN).pdf

view: pdf
	xdg-open $(MAIN).pdf >/dev/null 2>&1 &

clean:
	rm -rf $(BUILD_DIR)
	rm -f *.aux *.log *.out *.toc *.lof *.lot *.fls *.fdb_latexmk

clean-all: clean
	rm -f $(MAIN).pdf
